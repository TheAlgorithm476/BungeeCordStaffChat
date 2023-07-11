package me.thealgorithm476.bcsc.commands;

import me.thealgorithm476.bcsc.BCSC;
import me.thealgorithm476.bcsc.ConfigValues;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class StaffChatCommand extends Command {
    public StaffChatCommand(String name, String permission, String... aliases) { super (name, permission, aliases); }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&c&lOnly Staff Players can use this command!")));
            return;
        }

        if (!(player.hasPermission("bcsc.use"))) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&c&lYou don't have the right permission to run this command!")));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&aRunning BCSC by TheAlgorithm476")));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "Reloading...")));

            if (!(BCSC.reload())) {
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&c&lSomething went wrong reloading this plugin!")));
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&cIf this is your first time seeing this error, you can try to restart your Proxy, to see if this fixes it.")));
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&cIf the issue persists, please contact the Developer on Discord, at thealgorithm476. He will assist you in fixing this.")));
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&cAlternatively, you can open an issue on GitHub, at https://github.com/TheAlgorithm476/BungeeCordStaffChat.")));
                BCSC.getInstance().onDisable();
                return;
            }

            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "Successfully reloaded!")));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (!(ConfigValues.getInstance().modules_enabled_toggle)) {
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&cStaff Chat Toggle has been disabled from the config.")));
                return;
            }

            BCSC.toggleStaffChat(player);
            boolean toggled = BCSC.getToggleStatus(player);

            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&aToggled Staff Chat " + (toggled ? "on" : "off"))));
            return;
        }

        if (!(ConfigValues.getInstance().modules_enabled_command)) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().messageprefix + "&cCommand-Based Staff Chat has been disabled from the config.")));
            return;
        }

        String text = String.join(" ", args);

        ProxyServer.getInstance().getPlayers()
                .stream()
                .filter(target -> target.hasPermission("bcsc.use"))
                .forEach(target -> target.sendMessage(BCSC.composeMessage(player, text)));
    }
}