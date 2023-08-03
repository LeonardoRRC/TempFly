package me.theboykiss.ovh.tempfly;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TempFlyPlaceholderExpansion extends PlaceholderExpansion {

    private TempFlyCommand tempFlyCommand;

    public TempFlyPlaceholderExpansion(TempFlyCommand tempFlyCommand) {
        this.tempFlyCommand = tempFlyCommand;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tempfly";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheBoykissOld";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        if ("time_left".equals(identifier)) {
            return String.valueOf(tempFlyCommand.getFlyTime(player));
        }
        return null;
    }
}
