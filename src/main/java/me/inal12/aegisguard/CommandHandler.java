package me.inal12.aegisguard;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {

    private final AegisGuard plugin;

    public CommandHandler(AegisGuard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aegisguard.admin")) {
            sender.sendMessage(plugin.translate(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.translate(plugin.getConfig().getString("messages.invalid-args")));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(plugin.getPrefix() + plugin.translate(plugin.getConfig().getString("messages.reload")));
            return true;
        } else if (args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(ChatColor.GOLD + "--- AegisGuard Status ---");
            sender.sendMessage(ChatColor.YELLOW + "Sunucu koruma aktif.");
            sender.sendMessage(ChatColor.YELLOW + "Yazar: inal12");
            sender.sendMessage(ChatColor.YELLOW + "Versiyon: 1.0.0");
            sender.sendMessage(ChatColor.GOLD + "-------------------------");
            return true;
        } else {
            sender.sendMessage(plugin.translate(plugin.getConfig().getString("messages.invalid-args")));
        }

        return true;
    }
}
