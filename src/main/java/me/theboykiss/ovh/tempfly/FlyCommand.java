package me.theboykiss.ovh.tempfly;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {

    private TempFlyCommand tempFlyCommand;
    private ActionBarTask actionBarTask;

    public FlyCommand(TempFlyCommand tempFlyCommand) {
        this.tempFlyCommand = tempFlyCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fly")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
                return true;
            }
            Player player = (Player) sender;
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                player.sendMessage(tempFlyCommand.getMessage("flight-mode-disabled"));
            } else {
                if (tempFlyCommand.hasFlyTime(player) || player.hasPermission("tempfly.infinite")) {
                    player.setAllowFlight(true);
                    player.sendMessage(tempFlyCommand.getMessage("flight-mode-enabled"));
                    new ActionBarTask(player, tempFlyCommand).runTaskTimer(TempFly.getInstance(), 0L, 1L);
                } else {
                    player.sendMessage(tempFlyCommand.getMessage("no-flight-time-left"));
                }
            }
            return true;
        }
        return false;
    }
}
