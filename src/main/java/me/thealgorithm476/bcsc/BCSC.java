package me.thealgorithm476.bcsc;

import me.thealgorithm476.bcsc.commands.StaffChatCommand;
import me.thealgorithm476.bcsc.events.BcscEventListener;
import me.thealgorithm476.bcsc.metrics.Metrics;
import me.thealgorithm476.bcsc.util.LuckPermsUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BCSC extends Plugin {
    private static BCSC instance;

    private static final Map<UUID, Boolean> toggledStaffChats = new HashMap<>();

    @Override
    public void onLoad() { instance = this; }

    @Override
    public void onEnable() {
        this.showLogo();
        this.getLogger().info("Running on BungeeCord/" + this.getProxy().getName() + " " + this.getProxy().getVersion());

        File configFile = new File(this.getDataFolder(), "config.yml");

        if (!(configFile.exists())) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (stream == null) throw new IOException("Stream is null");
                if (!(this.getDataFolder().exists())) this.getDataFolder().mkdir();

                Files.copy(stream, configFile.toPath());
            } catch (IOException exception) {
                exception.printStackTrace();
                this.getLogger().warning("Something went wrong whilst loading the plugin! Disabling BCSC.");
                this.getLogger().warning("If this is your first time seeing this warning, then you may try restarting the proxy, to see if that fixes it.");
                this.getLogger().warning("If the issue persists, however, then consider contacting the developer on Discord, at thealgorithm476.");
                this.getLogger().warning("Alternatively, you can open an issue on GitHub, at https://github.com/TheAlgorithm476/BungeeCordStaffChat");
                this.onDisable();
                return;
            }
        }

        // Load config
        if (!(reload())) {
            onDisable();
            return;
        }

        this.getLogger().info("Registering Commands...");
        this.getProxy().getPluginManager().registerCommand(this, new StaffChatCommand("staffchat", "", "sc")); // not using the permission field here, since I've had some issues with it in the past.

        this.getLogger().info("Registering Events...");
        this.getProxy().getPluginManager().registerListener(this, new BcscEventListener());

        this.getLogger().info("Loading Dependencies...");
        this.loadDependencies();

        if (ConfigValues.getInstance().general_metrics) {
            Metrics metrics = new Metrics(this, 7547);

            metrics.addCustomChart(new Metrics.SimplePie("check_for_updates", () -> ConfigValues.getInstance().updates_check_for_updates ? "Enabled" : "Disabled"));
            metrics.addCustomChart(new Metrics.SimplePie("auto_update", () -> ConfigValues.getInstance().updates_auto_update ? "Enabled" : "Disabled"));
        }

        this.getLogger().info("BCSC " + this.getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        this.getProxy().getPluginManager().unregisterListeners(this);
        this.getProxy().getPluginManager().unregisterCommands(this);
    }

    private void showLogo() {
        this.getLogger().info("  _                   ");
        this.getLogger().info(" | |                  ");
        this.getLogger().info(" | |__   ___ ___  ___ ");
        this.getLogger().info(" | '_ \\ / __/ __|/ __|");
        this.getLogger().info(" | |_) | (__\\__ \\ (__ ");
        this.getLogger().info(" |_.__/ \\___|___/\\___|");
    }

    private void loadDependencies() {
        if (!(LuckPermsUtil.isLuckPermsInstalled())) getLogger().warning("LuckPerms is not installed! Prefixes, Suffixes, and Display Names may not work as expected!");
    }

    public static void toggleStaffChat(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();

        if (!(toggledStaffChats.containsKey(uuid))) {
            toggledStaffChats.put(uuid, true);
            return;
        }

        toggledStaffChats.replace(uuid, (!(toggledStaffChats.get(uuid))));
    }

    public static boolean getToggleStatus(ProxiedPlayer player) { return toggledStaffChats.getOrDefault(player.getUniqueId(), false); }

    public static boolean reload() {
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(instance.getDataFolder(), "config.yml"));
            ConfigValues.getInstance().populate(config);

            // This is a really dirty way of handling things, and will cause problems going forward,
            // but it's the best way I can think of at this time
            // I could use Bungee's builtin ConfigurationProvider.save function
            // But that will remove comments, which we don't want.
            if (ConfigValues.getInstance().general_config_version == 1) {
                instance.getLogger().warning("Config v1 detected! Upgrading to v2...");
                List<String> lines = new ArrayList<>();

                try (InputStream stream = BCSC.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (stream == null) throw new IOException("Stream is null");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String line;

                        while ((line = reader.readLine()) != null) lines.add(line);
                    }
                }

                String file = lines.stream().map(line -> line
                        .replace("config-version: 1", "config-version: 2")
                        .replace("metrics: true", "metrics: " + ConfigValues.getInstance().general_metrics)
                        .replace("prefix: true", "prefix: " + ConfigValues.getInstance().modules_enabled_prefix)
                        .replace("command: true", "command: " + ConfigValues.getInstance().modules_enabled_command)
                        .replace("prefix: \"#\"", "prefix: \"" + ConfigValues.getInstance().prefix + "\"")
                        .replace("messageprefix: \"[&b&lBCSC&r] &7> &r\"", "messageprefix: \"" + ConfigValues.getInstance().messageprefix + "\"")
                        .replace("staffchat: \"[&b&lBCSC&r] &b{DISPLAYNAME} &r[{SERVER}] &7> &r{MESSAGE}\"", "staffchat: \"" + ConfigValues.getInstance().staffchat + "\"")
                ).collect(Collectors.joining("\n"));
                Files.writeString(new File(instance.getDataFolder(), "config.yml").toPath(), file, StandardOpenOption.TRUNCATE_EXISTING);

                instance.getLogger().info("Upgrade to Config v2 completed.");
            }

            ConfigValues.getInstance().populate(config);

            if (ConfigValues.getInstance().general_config_version == 2) {
                instance.getLogger().warning("Config v2 detected! Upgrading to v3...");
                List<String> lines = new ArrayList<>();

                try (InputStream stream = BCSC.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (stream == null) throw new IOException("Stream is null");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String line;

                        while ((line = reader.readLine()) != null) lines.add(line);
                    }
                }

                String file = lines.stream().map(line -> line
                        .replace("metrics: true", "metrics: " + ConfigValues.getInstance().general_metrics)
                        .replace("prefix: true", "prefix: " + ConfigValues.getInstance().modules_enabled_prefix)
                        .replace("command: true", "command: " + ConfigValues.getInstance().modules_enabled_command)
                        .replace("toggle: true", "toggle: " + ConfigValues.getInstance().modules_enabled_toggle)
                        .replace("prefix: \"#\"", "prefix: \"" + ConfigValues.getInstance().prefix + "\"")
                        .replace("messageprefix: \"[&b&lBCSC&r] &7> &r\"", "messageprefix: \"" + ConfigValues.getInstance().messageprefix + "\"")
                        .replace("staffchat: \"[&b&lBCSC&r] &b{DISPLAYNAME} &r[{SERVER}] &7> &r{MESSAGE}\"", "staffchat: \"" + ConfigValues.getInstance().staffchat + "\"")
                ).collect(Collectors.joining("\n"));
                Files.writeString(new File(instance.getDataFolder(), "config.yml").toPath(), file, StandardOpenOption.TRUNCATE_EXISTING);

                instance.getLogger().info("Upgrade to Config v3 completed.");
            }

            ConfigValues.getInstance().populate(config);

            if (ConfigValues.getInstance().general_config_version == 3) {
                instance.getLogger().warning("Config v3 detected! Upgrading to v4...");
                List<String> lines = new ArrayList<>();

                try (InputStream stream = BCSC.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (stream == null) throw new IOException("Stream is null");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String line;

                        while ((line = reader.readLine()) != null) lines.add(line);
                    }
                }

                String file = lines.stream().map(line -> line
                        .replace("metrics: true", "metrics: " + ConfigValues.getInstance().general_metrics)
                        .replace("prefix: true", "prefix: " + ConfigValues.getInstance().modules_enabled_prefix)
                        .replace("command: true", "command: " + ConfigValues.getInstance().modules_enabled_command)
                        .replace("toggle: true", "toggle: " + ConfigValues.getInstance().modules_enabled_toggle)
                        .replace("prefix: \"#\"", "prefix: \"" + ConfigValues.getInstance().prefix + "\"")
                        .replace("messageprefix: \"[&b&lBCSC&r] &7> &r\"", "messageprefix: \"" + ConfigValues.getInstance().messageprefix + "\"")
                        .replace("staffchat: \"[&b&lBCSC&r] &b{DISPLAYNAME} &r[{SERVER}] &7> &r{MESSAGE}\"", "staffchat: \"" + ConfigValues.getInstance().staffchat + "\"")
                ).collect(Collectors.joining("\n"));
                Files.writeString(new File(instance.getDataFolder(), "config.yml").toPath(), file, StandardOpenOption.TRUNCATE_EXISTING);

                instance.getLogger().info("Upgrade to Config v3 completed.");
            }

            return true;
        } catch (IOException exception) {
            instance.getLogger().warning("Something went wrong whilst loading the config!");
            instance.getLogger().warning("If this is your first time seeing this warning, then you can try restarting your proxy, to see if this fixes it.");
            instance.getLogger().warning("If the issue persists, however, then please consider contacting the developer on Discord, at thealgorithm476");
            instance.getLogger().warning("Alternatively, you can open an issue on GitHub, at https://github.com/TheAlgorithm476/BungeeCordStaffChat");
            return false;
        }
    }

    public static TextComponent composeMessage(ProxiedPlayer player, String message) {
        String prefix = LuckPermsUtil.getPrefix(player);
        String suffix = LuckPermsUtil.getSuffix(player);

        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        return new TextComponent(ChatColor.translateAlternateColorCodes('&', ConfigValues.getInstance().staffchat)
                .replace("{PLAYERNAME}", player.getName())
                .replace("{DISPLAYNAME}", LuckPermsUtil.getDisplayName(player))
                .replace("{PREFIX}", prefix)
                .replace("{SUFFIX}", suffix)
                .replace("{SERVER}", player.getServer().getInfo().getName())
                .replace("{MESSAGE}", message)
        );
    }

    public static BCSC getInstance() { return instance; }
}
