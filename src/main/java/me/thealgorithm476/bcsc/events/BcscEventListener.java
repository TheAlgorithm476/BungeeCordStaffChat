package me.thealgorithm476.bcsc.events;

import me.thealgorithm476.bcsc.BCSC;
import me.thealgorithm476.bcsc.ConfigValues;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BcscEventListener implements Listener {
    @EventHandler
    public void onChat(ChatEvent event) {
        if (event.isCommand() || event.isProxyCommand()) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String message = event.getMessage();

        if (!(player.hasPermission("bcsc.use"))) return;

        if (ConfigValues.getInstance().modules_enabled_toggle && BCSC.getToggleStatus(player)) {
            event.setCancelled(true);

            ProxyServer.getInstance().getPlayers()
                    .stream()
                    .filter(target -> target.hasPermission("bcsc.use"))
                    .forEach(target -> target.sendMessage(BCSC.composeMessage(player, message)));
        }

        if (!(ConfigValues.getInstance().modules_enabled_prefix)) return;
        if (!(message.startsWith(ConfigValues.getInstance().prefix))) return;

        event.setCancelled(true);
        String text = message.replaceFirst(ConfigValues.getInstance().prefix, "");

        ProxyServer.getInstance().getPlayers()
                .stream()
                .filter(target -> target.hasPermission("bcsc.use"))
                .forEach(target -> target.sendMessage(BCSC.composeMessage(player, text)));
    }
}