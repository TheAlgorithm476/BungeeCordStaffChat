package me.thealgorithm476.bcsc;

import net.md_5.bungee.config.Configuration;

public class ConfigValues {
    private static ConfigValues instance;

    public byte general_config_version;
    public boolean general_metrics;
    public boolean modules_enabled_prefix;
    public boolean modules_enabled_command;
    public boolean modules_enabled_toggle;
    public String prefix;
    public String messageprefix;
    public String staffchat;
    public boolean updates_check_for_updates;
    public boolean updates_auto_update;

    private ConfigValues() {}

    public void populate(Configuration config) {
        this.general_config_version = config.getByte("general.config-version");
        this.general_metrics = config.getBoolean("general.metrics");
        this.modules_enabled_prefix = config.getBoolean("modules-enabled.prefix");
        this.modules_enabled_command = config.getBoolean("modules-enabled.command");
        this.modules_enabled_toggle = config.getBoolean("modules-enabled.toggle");
        this.prefix = config.getString("prefix");
        this.messageprefix = config.getString("messageprefix");
        this.staffchat = config.getString("staffchat");
        this.updates_check_for_updates = config.getBoolean("updates.check-for-updates");
        this.updates_auto_update = config.getBoolean("updates.auto-update");
    }

    public static ConfigValues getInstance() {
        if (instance == null) instance = new ConfigValues();
        return instance;
    }
}