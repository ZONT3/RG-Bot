package ru.rg.dsbot;

import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.ZDSBotBuilder;
import ru.zont.dsbot2.commands.implement.Help;
import ru.zont.dsbot2.commands.implement.exec.Cmd;
import ru.zont.dsbot2.commands.implement.exec.Do;
import ru.zont.dsbot2.commands.implement.exec.Exec;
import ru.zont.dsbot2.commands.implement.exec.Term;

import javax.security.auth.login.LoginException;
import java.util.Properties;

public class Main {

    private static String getVersion() {
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getResourceAsStream("/version.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.getProperty("version", "UNKNOWN");
    }

    public static class Config extends ru.zont.dsbot2.Config {
        public final Entry myChannel = new Entry("792009238062039070");
        public final Entry myRole = new Entry("777555228");

        @OverrideEntry
        public final Entry prefix = new Entry("//", true);
        @OverrideEntry
        public final Entry channel_log = new Entry("813383481241501748", true);
        @OverrideEntry
        public Entry version = new Entry(getVersion());
        @OverrideEntry
        public Entry version_str = new Entry("RG Spider v.%s");

        public Config() {
            super.prefix = prefix;
            super.channel_log = channel_log;
        }
    }

    public static void main(String[] args) throws LoginException {
        checkArgs(args);

        ZDSBot build = new ZDSBotBuilder(args[0])
                .defaultSetup()
                .setConfig(new Config())
                .addCommands(Help.class,
                        Exec.class, Cmd.class, Do.class, Term.class
                )
                .build();
    }

    private static void checkArgs(String[] args) throws LoginException {
        if (args.length < 2) throw new LoginException("API token and/or DB connection not provided!");
        Globals.dbConnection = args[1];
    }
}
