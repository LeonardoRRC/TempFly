package me.theboykiss.ovh.tempfly;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class BonusFlyTimeTask extends BukkitRunnable implements Listener {

    private TempFlyCommand tempFlyCommand;
    private File bonusFile;
    private YamlConfiguration bonusData;

    public BonusFlyTimeTask(TempFlyCommand tempFlyCommand, File dataFolder) {
        this.tempFlyCommand = tempFlyCommand;
        this.bonusFile = new File(dataFolder, "bonus.yml");
        if (!bonusFile.exists()) {
            try {
                bonusFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.bonusData = YamlConfiguration.loadConfiguration(bonusFile);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkAndGiveBonus(player);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkAndGiveBonus(player);
        }
    }

    private void checkAndGiveBonus(Player player) {
        for (int i = 1; i <= 86400; i++) {
            String permission = "tempfly.bonus." + i;
            if (player.hasPermission(permission)) {
                String uuid = player.getUniqueId().toString();
                long lastBonusTime = bonusData.getLong(uuid, 0);
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBonusTime >= 24 * 60 * 60 * 1000) {
                    tempFlyCommand.addFlyTime(player, i);
                    player.sendMessage("¡Has recibido tu bono diario de tiempo de vuelo!");
                    bonusData.set(uuid, currentTime);
                    try {
                        bonusData.save(bonusFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage("Tu proximo bonus sera dentro de 24 Horas.");
                }
                break;
            }
        }
    }
}
