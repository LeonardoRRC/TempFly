package me.theboykiss.ovh.tempfly;

import org.bukkit.scheduler.BukkitRunnable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.channel.TextChannel;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CouponTask extends BukkitRunnable {

    private TempFly plugin;

    public CouponTask(TempFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        String coupon = UUID.randomUUID().toString().substring(0, 8);
        plugin.getCoupons().put(coupon, 300);

        long channelId = plugin.getDiscordChannelId();
        String thumbnailUrl = plugin.getThumbnailUrl();
        TextChannel channel = plugin.getDiscordApi().getTextChannelById(channelId).orElse(null);
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("¡Nuevo cupón disponible!")
                    .setDescription("Canjéalo en nuestro servidor de Minecraft con `/tempfly canjear " + coupon + "` para obtener 5 minutos de vuelo.")
                    .setColor(Color.GREEN)
                    .setThumbnail(thumbnailUrl);

            channel.sendMessage(embed).thenAcceptAsync(message -> {
                try {
                    TimeUnit.MINUTES.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                message.delete();
            });
        }
    }
}
