package me.inal12.aegisguard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;

public class AegisGuard extends JavaPlugin implements Listener {

    private String prefix;
    private final Map<UUID, Long> chatCooldowns = new HashMap<>();
    private final Map<UUID, Long> commandCooldowns = new HashMap<>();

    // Config Cache
    public final Set<String> blockedWords = new HashSet<>();
    public final Set<String> blockedCommands = new HashSet<>();
    public final Set<String> superAdmins = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        getCommand("aegisguard").setExecutor(new CommandHandler(this));
        Bukkit.getPluginManager().registerEvents(new ProtectionListeners(this), this);

        sendStartupMessage();
    }

    public void loadSettings() {
        reloadConfig();
        prefix = translate(getConfig().getString("settings.prefix", "&e&lAegis&6&lGuard &8» &f"));

        // Cache words
        blockedWords.clear();
        getConfig().getStringList("chat-protection.blocked-words").forEach(s -> blockedWords.add(s.toLowerCase()));

        // Cache commands
        blockedCommands.clear();
        getConfig().getStringList("command-protection.blocked-commands")
                .forEach(s -> blockedCommands.add(s.toLowerCase()));

        // Cache super admins
        superAdmins.clear();
        superAdmins.addAll(getConfig().getStringList("security.super-admins"));
    }

    private void sendStartupMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "========================================");
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "AegisGuard v1.2 (Optimized)");
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Author: inal12");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Sistem %100 performans modunda aktif!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "========================================");
    }

    public String translate(String input) {
        return input == null ? "" : ChatColor.translateAlternateColorCodes('&', input);
    }

    public String getPrefix() {
        return prefix;
    }

    public Map<UUID, Long> getChatCooldowns() {
        return chatCooldowns;
    }

    public Map<UUID, Long> getCommandCooldowns() {
        return commandCooldowns;
    }

    public void reloadPlugin() {
        loadSettings();
    }
}
