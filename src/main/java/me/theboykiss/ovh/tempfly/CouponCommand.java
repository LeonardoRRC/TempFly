package me.theboykiss.ovh.tempfly;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CouponCommand implements CommandExecutor {

    private TempFlyCommand tempFlyCommand;

    public CouponCommand(TempFlyCommand tempFlyCommand) {
        this.tempFlyCommand = tempFlyCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tempFlyCommand.getMessage("no-console"));
            return true;
        }
        Player player = (Player) sender;
        if (sender.hasPermission("tempfly.cupon.crear")){
            if (args.length == 3 && args[0].equalsIgnoreCase("crear") && args[1].equalsIgnoreCase("cupon")) {
                int flightTime;
                try {
                    flightTime = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(tempFlyCommand.getMessage("invalid-number2"));
                    return true;
                }
                String coupon = RandomStringUtils.randomAlphanumeric(8);
                TempFly.getInstance().getCoupons().put(coupon, flightTime);
                sender.sendMessage(String.format(tempFlyCommand.getMessage("coupon-created"), coupon));
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("canjear")) {
                String coupon = args[1];
                Integer flightTime = TempFly.getInstance().getCoupons().remove(coupon);
                if (flightTime == null) {
                    sender.sendMessage(tempFlyCommand.getMessage("cupon-novalid"));
                    return true;
                }
                tempFlyCommand.addFlyTime(player, flightTime);
                sender.sendMessage(String.format(tempFlyCommand.getMessage("coupon-redeemed"), flightTime));
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
