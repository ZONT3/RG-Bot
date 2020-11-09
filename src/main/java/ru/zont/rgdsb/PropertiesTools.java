package ru.zont.rgdsb;

import static ru.zont.rgdsb.InteractAdapter.getGlobalProps;

public class PropertiesTools {
    public static String getRoleGmID() {
        return getGlobalProps().getProperty("ROLE_GM");
    }

    public static String getChannelStatusID() {
        return getGlobalProps().getProperty("CHANNEL_STATUS");
    }

    static String getPlayerRoleID() {
        return getGlobalProps().getProperty("ROLE_PLAYER");
    }

    static String getChannelPlayersID() {
        return getGlobalProps().getProperty("CHANNEL_PLAYERS");
    }
}
