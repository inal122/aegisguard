package me.inal12.aegisguard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;

public class ProtectionListeners implements Listener {

    private final AegisGuard plugin;

    public ProtectionListeners(AegisGuard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (type == EntityType.PRIMED_TNT && plugin.getConfig().getBoolean("antigrief.block-tnt-explosion")) {
            event.blockList().clear();
        } else if (type == EntityType.CREEPER && plugin.getConfig().getBoolean("antigrief.block-creeper-explosion")) {
            event.blockList().clear();
        } else if (type == EntityType.WITHER_SKULL && plugin.getConfig().getBoolean("antigrief.block-wither-spawn")) {
            event.blockList().clear();
        } else if (type == EntityType.ENDER_CRYSTAL
                && plugin.getConfig().getBoolean("antigrief.block-end-crystal-explosion")) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (type == EntityType.WITHER && plugin.getConfig().getBoolean("antigrief.block-wither-spawn")) {
            event.setCancelled(true);
        } else if (type == EntityType.ENDER_DRAGON && plugin.getConfig().getBoolean("antigrief.block-dragon-spawn")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() == org.bukkit.Material.FIRE
                && plugin.getConfig().getBoolean("antigrief.block-fire-spread")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (isSuperAdmin(player) || player.hasPermission("aegisguard.bypass.chatslow"))
            return;

        if (plugin.getConfig().getBoolean("chat-protection.enabled")) {
            long cooldown = plugin.getConfig().getLong("chat-protection.cooldown") * 1000L;
            long now = System.currentTimeMillis();
            long lastChat = plugin.getChatCooldowns().getOrDefault(player.getUniqueId(), 0L);

            if (now - lastChat < cooldown) {
                event.setCancelled(true);
                long left = (cooldown - (now - lastChat)) / 1000L + 1;
                player.sendMessage(
                        plugin.getPrefix() + plugin.translate(plugin.getConfig().getString("chat-protection.message")
                                .replace("%seconds%", String.valueOf(left))));
                return;
            }
            plugin.getChatCooldowns().put(player.getUniqueId(), now);
        }

        String[] words = event.getMessage().toLowerCase().split("\\s+");
        for (String word : words) {
            if (plugin.blockedWords.contains(word)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + ChatColor.RED + "Lütfen kelimelerinize dikkat edin!");
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String baseCmd = message.split(" ")[0].toLowerCase();

        if (plugin.getConfig().getBoolean("security.block-colon-commands") && baseCmd.contains(":")) {
            if (!player.hasPermission("aegisguard.admin")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getPrefix() + ChatColor.RED + "Geçersiz komut biçimi!");
                return;
            }
        }

        if (baseCmd.equals("/op") || baseCmd.equals("/deop") || baseCmd.equals("/stop") || baseCmd.equals("/reload")
                || baseCmd.equals("/rl")) {
            if (!isSuperAdmin(player)) {
                event.setCancelled(true);
                player.sendMessage(plugin.translate(plugin.getConfig().getString("security.unauthorized-msg")));
                return;
            }
        }

        if (plugin.getConfig().getBoolean("mod-tools.command-spy")) {
            String spyMsg = plugin.translate(plugin.getConfig().getString("mod-tools.spy-format")
                    .replace("%player%", player.getName()).replace("%command%", message));
            Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("aegisguard.admin") && p != player)
                    .forEach(p -> p.sendMessage(spyMsg));
        }

        if (isSuperAdmin(player) || player.hasPermission("aegisguard.bypass.commandslow"))
            return;

        if (plugin.getConfig().getBoolean("command-protection.enabled")) {
            long cooldown = plugin.getConfig().getLong("command-protection.cooldown") * 1000L;
            long now = System.currentTimeMillis();
            long lastCmd = plugin.getCommandCooldowns().getOrDefault(player.getUniqueId(), 0L);

            if (now - lastCmd < cooldown) {
                event.setCancelled(true);
                long left = (cooldown - (now - lastCmd)) / 1000L + 1;
                player.sendMessage(
                        plugin.getPrefix() + plugin.translate(plugin.getConfig().getString("command-protection.message")
                                .replace("%seconds%", String.valueOf(left))));
                return;
            }
            plugin.getCommandCooldowns().put(player.getUniqueId(), now);
        }

        if (plugin.blockedCommands.contains(baseCmd)) {
            if (!player.hasPermission("aegisguard.admin")) {
                event.setCancelled(true);
                player.sendMessage(plugin.translate(plugin.getConfig().getString("messages.no-permission")));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSecurityJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && !isSuperAdmin(player)) {
            if (plugin.getConfig().getBoolean("security.auto-deop-unauthorized")) {
                player.setOp(false);
                if (plugin.getConfig().getBoolean("security.kick-unauthorized-op"))
                    player.kickPlayer(plugin.translate(plugin.getConfig().getString("security.unauthorized-msg")));
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.DARK_RED + "!!! IZINSIZ OP GIRISI: " + player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getConfig().getBoolean("velocity.enabled"))
            return;
        if (!event.getAddress().getHostAddress().equals(plugin.getConfig().getString("velocity.proxy-ip"))) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    plugin.translate(plugin.getConfig().getString("velocity.kick-message")));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("antibot.enabled"))
            return;
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        long count = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getAddress().getAddress().getHostAddress().equals(ip)).count();
        if (count > plugin.getConfig().getInt("antibot.limit-per-ip")) {
            event.getPlayer().kickPlayer(plugin.translate(plugin.getConfig().getString("antibot.kick-message")
                    .replace("%limit%", String.valueOf(plugin.getConfig().getInt("antibot.limit-per-ip")))));
        }
    }

    private boolean isSuperAdmin(Player player) {
        return plugin.superAdmins.contains(player.getName());
    }
}
