package me.theboykiss.ovh.tempfly;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class TempFlyCommand implements CommandExecutor {

    private DatabaseManager databaseManager;
    private HashMap<UUID, Integer> flyTime = new HashMap<>();
    private File dataFile;
    private TempFly plugin;
    private YamlConfiguration data;
    private YamlConfiguration messages;

    public TempFlyCommand(DatabaseManager databaseManager, File dataFile, YamlConfiguration messages, TempFly plugin) {
        this.databaseManager = databaseManager;
        this.dataFile = dataFile;
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.messages = messages;
        this.plugin = plugin;
        startFlyTimer();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tempfly") && sender.hasPermission("tempfly.admin.commands")) {
            if (args.length == 3) {
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(getMessage("player-not-online"));
                    return true;
                }
                int time;
                try {
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(getMessage("invalid-number"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("set")) {
                    setFlyTime(target, time);
                    sender.sendMessage(ChatColor.GREEN+ "Has establecido " + time + " al jugador " + target);
                } else if (args[0].equalsIgnoreCase("give")) {
                    addFlyTime(target, time);
                    sender.sendMessage(ChatColor.GREEN+ "Has agregado " + time + " al jugador " + target);
                }
            }
            return true;
        }
        return false;
    }

    //public void setFlyTime(Player player, int time) {
    //    flyTime.put(player.getUniqueId(), time);
    //}

    public void setFlyTime(Player player, int time) {
        UUID uuid = player.getUniqueId();
        if (databaseManager.isSQLEnabled()) {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE fly_time SET time = ? WHERE uuid = ?")) {
                statement.setInt(1, time);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            flyTime.put(uuid, time);
        }
    }


    //public void addFlyTime(Player player, int time) {
    //    flyTime.put(player.getUniqueId(), flyTime.getOrDefault(player.getUniqueId(), 0) + time);
    //}

    public void addFlyTime(Player player, int time) {
        UUID uuid = player.getUniqueId();
        if (databaseManager.isSQLEnabled()) {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE fly_time SET time = time + ? WHERE uuid = ?")) {
                statement.setInt(1, time);
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            flyTime.put(uuid, flyTime.getOrDefault(uuid, 0) + time);
        }
    }


    private void startFlyTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    int timeLeft = getFlyTime(player);
                    if (player.isFlying()) {
                        if (timeLeft <= 0) {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            if (databaseManager.isSQLEnabled()) {
                                try (Connection connection = databaseManager.getConnection();
                                     PreparedStatement statement = connection.prepareStatement("DELETE FROM fly_time WHERE uuid = ?")) {
                                    statement.setString(1, uuid.toString());
                                    statement.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                flyTime.remove(uuid);
                            }
                        } else if (timeLeft <= 10) {
                            player.sendTitle(getMessage("attention-title"), getMessage("flight-time-left").replace("{time}", String.valueOf(timeLeft)), 10, 70, 20);
                            setFlyTime(player, timeLeft - 1);
                        } else {
                            setFlyTime(player, timeLeft - 1);
                        }
                    }

                }
            }
        }.runTaskTimer(TempFly.getInstance(), 0L, 20L);
    }


    public boolean hasFlyTime(Player player) {
        int time = flyTime.getOrDefault(player.getUniqueId(), 0);
        return time > 0;
    }



    /*public void saveData() {
        for (UUID uuid : flyTime.keySet()) {
            data.set(uuid.toString(), flyTime.get(uuid));
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void saveData() {
        if (databaseManager.isSQLEnabled()) {
            // Guardar datos en MySQL
            try (Connection connection = databaseManager.getConnection()) {
                for (UUID uuid : flyTime.keySet()) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE fly_time SET time = ? WHERE uuid = ?")) {
                        statement.setInt(1, flyTime.get(uuid));
                        statement.setString(2, uuid.toString());
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Guardar datos en archivo local
            for (UUID uuid : flyTime.keySet()) {
                data.set(uuid.toString(), flyTime.get(uuid));
            }
            try {
                data.save(dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //public void loadData() {
    //    for (String key : data.getKeys(false)) {
    //        flyTime.put(UUID.fromString(key), data.getInt(key));
    //    }
    //}

    public void loadData() {
        if (databaseManager.isSQLEnabled()) {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT uuid, time FROM fly_time");
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    int time = resultSet.getInt("time");
                    flyTime.put(uuid, time);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            for (String key : data.getKeys(false)) {
                flyTime.put(UUID.fromString(key), data.getInt(key));
            }
        }
    }


    public String getMessage(String key) {
        if (messages.contains(key)) {
            return ChatColor.translateAlternateColorCodes('&', messages.getString(key));
        } else {
            return ChatColor.RED + "Error: No se pudo encontrar el mensaje '" + key + "'.";
        }
    }

    public int getFlyTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (databaseManager.isSQLEnabled()) {
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT time FROM fly_time WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("time");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            return flyTime.getOrDefault(uuid, 0);
        }
    }

}
