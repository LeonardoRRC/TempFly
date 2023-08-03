package me.theboykiss.ovh.tempfly;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBarTask extends BukkitRunnable {

    private Player player;
    private TempFlyCommand tempFlyCommand;

    public ActionBarTask(Player player, TempFlyCommand tempFlyCommand) {
        this.player = player;
        this.tempFlyCommand = tempFlyCommand;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }
        int timeLeft = tempFlyCommand.getFlyTime(player);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(String.format(tempFlyCommand.getMessage("action-bar-timeleft"), timeLeft)));
    }
}
