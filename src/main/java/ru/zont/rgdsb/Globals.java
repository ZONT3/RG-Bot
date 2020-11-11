package ru.zont.rgdsb;

import ru.zont.rgdsb.listeners.LPlayersMonitoring;
import ru.zont.rgdsb.listeners.LServerStatus;

public class Globals {
    public static String dbConnection = "";

    public static InteractAdapter[] commandAdapters = null;

    public static String ZONT_MENTION = "<@331524458806247426>";

    public static LPlayersMonitoring playersMonitoring;
    public static LServerStatus serverStatus;
}
