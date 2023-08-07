package me.theboykiss.ovh.tempfly;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
public class TempFly extends JavaPlugin {

    private static TempFly instance;
    private HashMap<String, Integer> coupons = new HashMap<>();
    private TempFlyCommand tempFlyCommand;
    private DiscordApi discordApi;
    private HikariDataSource dataSource;
    private YamlConfiguration sqlConfig;
    private boolean sqlEnabled;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File sqlFile = new File(getDataFolder(), "sql.yml");
        if (!sqlFile.exists()) {
            saveResource("sql.yml", false);
        }
        DatabaseManager databaseManager = new DatabaseManager(sqlFile);
        if (databaseManager.isSQLEnabled()) {
            databaseManager.createTableIfNotExists();
        }
        new CouponTask(this).runTaskTimer(this, 0L, 20L * 60L * 60L);
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File dataFile = new File(getDataFolder(), "data.yml");
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
                setupDefaultMessages(messagesFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        tempFlyCommand = new TempFlyCommand(databaseManager, dataFile, messages, this);
        tempFlyCommand.loadData();
        this.getCommand("tempfly").setExecutor(tempFlyCommand);
        getCommand("tempflyc").setExecutor(new CouponCommand(tempFlyCommand));
        this.getCommand("fly").setExecutor(new FlyCommand(tempFlyCommand));
        BonusFlyTimeTask bonusFlyTimeTask = new BonusFlyTimeTask(tempFlyCommand, getDataFolder());
        //bonusFlyTimeTask.runTaskTimer(this, 0L, 20L * 60L * 60L);
        getServer().getPluginManager().registerEvents(bonusFlyTimeTask, this);
        new TempFlyPlaceholderExpansion(tempFlyCommand).register();
        discordApi = new DiscordApiBuilder().setToken(this.getDiscordToken()).login().join();
    }


    @Override
    public void onDisable() {
        if (discordApi != null) {
            discordApi.disconnect();
        }
        tempFlyCommand.saveData();
        instance = null;
        databaseManager.close();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public static TempFly getInstance() {
        return instance;
    }

    private void setupDefaultMessages(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("player-not-online", "&cEl jugador no está en línea.");
        config.set("invalid-number", "&cPor favor, introduce un número válido.");
        config.set("flight-mode-disabled", "&aModo de vuelo desactivado.");
        config.set("flight-mode-enabled", "&aModo de vuelo activado.");
        config.set("no-flight-time-left", "&cNo tienes tiempo de vuelo restante.");
        config.set("attention-title", "&6¡Atención!");
        config.set("flight-time-left", "&eTe quedan {time} segundos de vuelo");
        config.set("no-console", "&cNo puedes ejecutar ese comando por consola.");
        config.set("invalid-number2", "El tiempo de vuelo debe ser un número entero.");
        config.set("coupon-created", "&aTu cupón es: %s");
        config.set("cupon-novalid", "Ese cupón no es válido.");
        config.set("coupon-redeemed", "&aHas canjeado el cupón por %s segundos de tiempo de vuelo.");
        config.set("action-bar-timeleft", "&aTiempo Restante %s Segundos");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Integer> getCoupons() {
        return coupons;
    }
    public DiscordApi getDiscordApi() {
        return discordApi;
    }

    public String getDiscordToken() {
        return getConfig().getString("discord.token");
    }

    public long getDiscordChannelId() {
        return getConfig().getLong("discord.channel-id");
    }

    public String getThumbnailUrl() {
        return getConfig().getString("discord.thumbnail-url");
    }

    public Connection getDatabaseConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

}
