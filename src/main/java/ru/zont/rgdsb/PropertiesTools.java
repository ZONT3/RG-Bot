package ru.zont.rgdsb;

import java.io.File;

import static ru.zont.rgdsb.InteractAdapter.getGlobalProps;

public class PropertiesTools {
    public static final File DIR_PROPS = new File("properties");

    public static String getRoleGmID() {
        String res = getGlobalProps().getProperty("ROLE_GM");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static String getChannelStatusID() {
        String res = getGlobalProps().getProperty("CHANNEL_STATUS");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    static String getPlayerRoleID() {
        String res = getGlobalProps().getProperty("ROLE_PLAYER");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    static String getChannelPlayersID() {
        String res = getGlobalProps().getProperty("CHANNEL_PLAYERS");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static class IdNotProvidedException extends RuntimeException {
        public IdNotProvidedException(String s) {
            super(s);
        }

        public IdNotProvidedException() {
            this("Please check global config entries with prefixes `ROLE_` and `CHANNEL_`");
        }
    }
}
