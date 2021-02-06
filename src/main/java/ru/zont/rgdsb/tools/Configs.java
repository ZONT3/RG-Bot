package ru.zont.rgdsb.tools;

import static ru.zont.dsbot.core.tools.Configs.*;

public class Configs {

    public static String getRolePlayerID() {
        return getID("ROLE_PLAYER");
    }

    public static String getChannelPlayersID() {
        return getID("CHANNEL_PLAYERS");
    }

    public static String getChannelStatusID() {
        return getID("CHANNEL_STATUS");
    }

    public static String getRoleGmmID() {
        return getID("ROLE_GMM");
    }

    public static String getRoleGmID() {
        return getID("ROLE_GM");
    }

    public static String getRoleExecutorID() {
        return getID("ROLE_EXECUTOR");
    }
}
