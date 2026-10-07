package com.camdaloon.simpleflags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UploadListener implements Listener {
    private final SimpleFlags plugin;
    private final FlagService service;
    private static final Map<UUID, FlagEditor> pending = new ConcurrentHashMap<>();

    public UploadListener(SimpleFlags plugin, FlagService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void begin(Player player, FlagEditor editor) {
        pending.put(player.getUniqueId(), editor);
        player.sendMessage(ChatColor.YELLOW + "Type a direct image URL in chat, or type cancel.");
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent event) {
        FlagEditor editor = pending.remove(event.getPlayer().getUniqueId());
        if (editor == null) return;
        event.setCancelled(true);
        String message = event.getMessage().trim();
        if (message.equalsIgnoreCase("cancel")) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Image upload cancelled.");
            return;
        }
        Player player = event.getPlayer();
        player.sendMessage(ChatColor.YELLOW + "Downloading image...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URI uri = URI.create(message);
                if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) throw new IllegalArgumentException();
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("User-Agent", "SimpleFlags/1.0.0-4_beta");
                try (InputStream stream = connection.getInputStream()) {
                    BufferedImage source = ImageIO.read(stream);
                    if (source == null) throw new IllegalArgumentException();
                    BufferedImage scaled = new BufferedImage(FlagService.WIDTH, FlagService.HEIGHT, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = scaled.createGraphics();
                    graphics.drawImage(source.getScaledInstance(FlagService.WIDTH, FlagService.HEIGHT, Image.SCALE_SMOOTH), 0, 0, null);
                    graphics.dispose();
                    int[] pixels = new int[FlagService.CELLS];
                    for (int y = 0; y < FlagService.HEIGHT; y++) {
                        for (int x = 0; x < FlagService.WIDTH; x++) pixels[y * FlagService.WIDTH + x] = scaled.getRGB(x, y) & 0xFFFFFF;
                    }
                    String design = service.encode(pixels);
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        editor.setDesign(design);
                        editor.refresh();
                        player.sendMessage(ChatColor.GREEN + "Image imported.");
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Unable to import that image URL."));
            }
        });
    }
}
