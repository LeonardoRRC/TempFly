package me.theboykiss.ovh.tempfly;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TempFly extends JavaPlugin {

    private static TempFly instance;
    private HashMap<String, Integer> coupons = new HashMap<>();
    private TempFlyCommand tempFlyCommand;

    @Override
    public void onEnable() {
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
        tempFlyCommand = new TempFlyCommand(dataFile, messages);
        tempFlyCommand.loadData();
        this.getCommand("tempfly").setExecutor(tempFlyCommand);
        getCommand("tempfly").setExecutor(new CouponCommand(tempFlyCommand));
        this.getCommand("fly").setExecutor(new FlyCommand(tempFlyCommand));
        new BonusFlyTimeTask(tempFlyCommand).runTaskTimer(this, 0L, 24 * 60 * 60 * 20L);
        new TempFlyPlaceholderExpansion(tempFlyCommand).register();
    }

    @Override
    public void onDisable() {
        tempFlyCommand.saveData();
        instance = null;
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
}
