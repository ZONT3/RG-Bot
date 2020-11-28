package ru.zont.rgdsb.tools;

import ru.zont.rgdsb.UTF8Control;

import java.util.ResourceBundle;

public class Strings {
    public static ResourceBundle STR = ResourceBundle.getBundle("strings", new UTF8Control());

    public static String countPlayers(int count) {
        return getPlural(count, STR.getString("plurals.players.other"), STR.getString("plurals.players.few"), STR.getString("plurals.players.other"));
    }

    public static String countGMs(int count) {
        return getPlural(count, STR.getString("plurals.gms.one"), STR.getString("plurals.gms.few"), STR.getString("plurals.gms.other"));
    }

    public static String getPlural(int count, String one, String few, String other) {
        int c = (count % 100);

        if (c == 1 || (c > 20 && c % 10 == 1))
            return String.format(one, count);
        if ((c < 10 || c > 20) && c % 10 >= 2 && c % 10 <= 4)
            return String.format(few, count);
        return String.format(other, count);
    }
}
