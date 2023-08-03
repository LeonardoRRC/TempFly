package me.theboykiss.ovh.tempfly;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BonusFlyTimeTask extends BukkitRunnable {

    private TempFlyCommand tempFlyCommand;

    public BonusFlyTimeTask(TempFlyCommand tempFlyCommand) {
        this.tempFlyCommand = tempFlyCommand;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 1; i <= 86400; i++) {
                if (player.hasPermission("tempfly.bonus." + i)) {
                    tempFlyCommand.addFlyTime(player, i);
                    break;
                }
            }
        }
    }
}
