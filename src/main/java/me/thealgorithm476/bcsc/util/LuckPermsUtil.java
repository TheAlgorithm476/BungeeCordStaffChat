package me.thealgorithm476.bcsc.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LuckPermsUtil {
    private static boolean installed;
    private static LuckPerms luckPerms;

    private LuckPermsUtil() {}

    @Nullable
    public static String getPrefix(@Nullable ProxiedPlayer player) {
        User user = getUser(player);

        if (user == null) return null;

        String prefix = user.getCachedData().getMetaData().getPrefix();

        if (prefix == null) return null;

        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    @Nullable
    public static String getSuffix(ProxiedPlayer player) {
        User user = getUser(player);

        if (user == null) return null;

        String suffix = user.getCachedData().getMetaData().getSuffix();

        if (suffix == null) return null;

        return ChatColor.translateAlternateColorCodes('&', suffix);
    }

    @NotNull
    public static String getDisplayName(ProxiedPlayer player) {
        User user = getUser(player);

        if (user == null) return player.getDisplayName();

        String prefix = getPrefix(player);
        String suffix = getSuffix(player);
        String name = player.getName();

        return ChatColor.translateAlternateColorCodes('&', String.format("%s%s%s", prefix == null ? "" : prefix, name, suffix == null ? "" : suffix));
    }

    @Nullable
    private static User getUser(@Nullable ProxiedPlayer player) {
        if (!(installed)) return null;
        if (player == null) return null;

        return luckPerms.getUserManager().getUser(player.getUniqueId());
    }

    public static boolean isLuckPermsInstalled() { return installed; }

    static {
        try {
            LuckPermsUtil.luckPerms = LuckPermsProvider.get();
            LuckPermsUtil.installed = true;
        } catch (IllegalStateException | NoClassDefFoundError throwable) {
            LuckPermsUtil.installed = false;
        }
    }
}