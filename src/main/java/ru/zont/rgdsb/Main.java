package ru.zont.rgdsb;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Configs;
import ru.zont.rgdsb.command.*;
import ru.zont.rgdsb.command.exec.Cmd;
import ru.zont.rgdsb.command.exec.Do;
import ru.zont.rgdsb.command.exec.Exec;
import ru.zont.rgdsb.command.exec.Term;
import ru.zont.rgdsb.listeners.LPlayersMonitoring;
import ru.zont.rgdsb.listeners.LServerStatus;
import ru.zont.rgdsb.tools.Globals;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length < 2) throw new LoginException("API token and/or DB connection not provided!");
        Globals.dbConnection = args[1];

        Configs.setGlobalPropsDefaults(new Properties(){{
            setProperty("ROLE_PLAYER", "0");
            setProperty("ROLE_GM", "0");
            setProperty("ROLE_GGM", "0");
            setProperty("ROLE_EXECUTOR", "0");
            setProperty("CHANNEL_PLAYERS", "0");
            setProperty("CHANNEL_STATUS", "0");
            setProperty("command_prefix", "//");
            setProperty("TA_IDS", "331524458806247426");
        }});

        final ZDSBot zdsBot = new ZDSBot(args[0], Globals.version, commands(), handlers());
        zdsBot.getJdaBuilder()
                .addEventListeners(Globals.serverStatus = new LServerStatus())
                .addEventListeners(new LPlayersMonitoring());
        zdsBot.create().awaitReady();
    }

    private static List<Class<? extends LStatusHandler>> handlers() {
        return Collections.emptyList();
    }

    private static List<Class<? extends CommandAdapter>> commands() {
        return Arrays.asList(
                Cmd.class,
                Do.class,
                Exec.class,
                Term.class,
                Config.class,
                GMs.class,
                Help.class,
                Ping.class,
                Say.class,
                Status.class
        );
    }
}
