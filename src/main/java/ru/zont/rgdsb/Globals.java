package ru.zont.rgdsb;

import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.listeners.LPlayersMonitoring;
import ru.zont.rgdsb.listeners.LServerStatus;

public class Globals {
    public static final String version = "1.6.4";

    public static String dbConnection = "";

    public static CommandAdapter[] commandAdapters = null;

    public static String ZONT_MENTION = "<@331524458806247426>";

    public static LPlayersMonitoring playersMonitoring;
    public static LServerStatus serverStatus;
}
