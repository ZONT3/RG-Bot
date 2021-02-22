package ru.rg.dsbot.tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.tools.CRouter;

import java.util.List;

import static ru.rg.dsbot.Strings.*;
import static ru.zont.dsbot2.tools.ZDSBStrings.getPlural;

public class Tools {

    public static String trimNick(String nick) {
        return nick.replaceAll("\\[.+]", "")
                .trim();
    }

    public static String countPlayers(int count) {
        return getPlural(count, STR.getString("plurals.players.other"), STR.getString("plurals.players.few"), STR.getString("plurals.players.other"));
    }

    public static String countGMs(int count) {
        return getPlural(count, STR.getString("plurals.gms.one"), STR.getString("plurals.gms.few"), STR.getString("plurals.gms.other"));
    }

    public static boolean isWindows() {
        return (System.getProperty("os.name").contains("win"));
    }

    public static boolean isMac() {
        return (System.getProperty("os.name").contains("mac"));
    }

    public static boolean isUnix() {
        final String os = System.getProperty("os.name");
        return (os.contains("nix")
                || os.contains("nux")
                || os.contains("aix"));
    }
}
