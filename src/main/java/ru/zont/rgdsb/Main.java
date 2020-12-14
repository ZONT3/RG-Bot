package ru.zont.rgdsb;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.zont.dsbot.core.tools.Configs;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.rgdsb.listeners.LPlayersMonitoring;
import ru.zont.rgdsb.listeners.LServerStatus;
import ru.zont.rgdsb.tools.Globals;

import javax.security.auth.login.LoginException;
import java.util.Properties;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length < 2) throw new LoginException("API token and/or DB connection not provided!");
        Globals.dbConnection = args[1];

        Configs.setGlobalPropsDefaults(new Properties(){{
            setProperty("ROLE_PLAYER", "0");
            setProperty("ROLE_GM", "0");
            setProperty("ROLE_GGM", "0");
            setProperty("CHANNEL_PLAYERS", "0");
            setProperty("CHANNEL_STATUS", "0");
            setProperty("command_prefix", "//");
            setProperty("TA_IDS", "331524458806247426");
        }});

        final ZDSBot zdsBot = new ZDSBot(args[0], Globals.version, "ru.zont.rgdsb.command", "ru.zont.rgdsb.listeners");
        zdsBot.getJdaBuilder()
                .addEventListeners(Globals.serverStatus = new LServerStatus())
                .addEventListeners(new LPlayersMonitoring());
        zdsBot.create().awaitReady();
    }
}
