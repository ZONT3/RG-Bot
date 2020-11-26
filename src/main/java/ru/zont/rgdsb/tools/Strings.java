package ru.zont.rgdsb.tools;

import ru.zont.rgdsb.UTF8Control;

import java.util.ResourceBundle;

public class Strings {
    public static ResourceBundle STR = ResourceBundle.getBundle("strings", new UTF8Control());

    static String countPlayers(int count) {
        int ccount =(count % 100);
        if ((ccount < 10 || ccount > 20) && ccount % 10 >= 2 && ccount % 10 <= 4)
            return String.format(STR.getString("plurals.players.few"), count);
        else return String.format(STR.getString("plurals.players.other"), count);
    }
}
