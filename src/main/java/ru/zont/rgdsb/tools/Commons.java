package ru.zont.rgdsb.tools;

import static ru.zont.dsbot.core.Strings.*;

public class Commons {
    public static String trimNick(String nick) {
        return nick.replaceAll("[\"']", "").trim()
                .replaceAll("\\[.+] *", "")
                .replaceAll("[ .]+.\\...?", "");
    }

    public static String countPlayers(int count) {
        return getPlural(count, STR.getString("plurals.players.other"), STR.getString("plurals.players.few"), STR.getString("plurals.players.other"));
    }

    public static String countGMs(int count) {
        return getPlural(count, STR.getString("plurals.gms.one"), STR.getString("plurals.gms.few"), STR.getString("plurals.gms.other"));
    }
}
