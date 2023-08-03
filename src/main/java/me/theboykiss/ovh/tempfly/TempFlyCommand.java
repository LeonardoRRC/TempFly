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
import java.util.HashMap;
import java.util.UUID;

public class TempFlyCommand implements CommandExecutor {

    private HashMap<UUID, Integer> flyTime = new HashMap<>();
    private File dataFile;
    private YamlConfiguration data;
    private YamlConfiguration messages;

    public TempFlyCommand(File dataFile, YamlConfiguration messages) {
        this.dataFile = dataFile;
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.messages = messages;
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
                } else if (args[0].equalsIgnoreCase("give")) {
                    addFlyTime(target, time);
                }
            }
            return true;
        }
        return false;
    }

    public void setFlyTime(Player player, int time) {
        flyTime.put(player.getUniqueId(), time);
    }

    public void addFlyTime(Player player, int time) {
        flyTime.put(player.getUniqueId(), flyTime.getOrDefault(player.getUniqueId(), 0) + time);
    }

    private void startFlyTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isFlying() && flyTime.containsKey(player.getUniqueId())) {
                        int timeLeft = flyTime.get(player.getUniqueId());
                        if (timeLeft <= 0) {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            flyTime.remove(player.getUniqueId());
                        } else if (timeLeft <= 10) {
                            player.sendTitle(getMessage("attention-title"), getMessage("flight-time-left").replace("{time}", String.valueOf(timeLeft)), 10, 70, 20);
                            flyTime.put(player.getUniqueId(), timeLeft - 1);
                        } else {
                            flyTime.put(player.getUniqueId(), timeLeft - 1);
                        }
                    }
                }
            }
        }.runTaskTimer(TempFly.getInstance(), 0L, 20L);
    }

    public boolean hasFlyTime(Player player) {
        return flyTime.containsKey(player.getUniqueId());
    }

    public void saveData() {
        for (UUID uuid : flyTime.keySet()) {
            data.set(uuid.toString(), flyTime.get(uuid));
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        for (String key : data.getKeys(false)) {
            flyTime.put(UUID.fromString(key), data.getInt(key));
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
        return flyTime.getOrDefault(player.getUniqueId(), 0);
    }

}
