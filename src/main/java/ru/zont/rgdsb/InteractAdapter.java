package ru.zont.rgdsb;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static ru.zont.rgdsb.Main.*;

public abstract class InteractAdapter {
    private static final String PROPS_COMMENT = "Properties of Right Games project's DS Bot Command";
    private static final int CACHE_LIFETIME = 20000;

    private Properties propertiesCache = null;
    private long propertiesCacheTS = 0;

    private static Properties gPropertiesCache = null;
    private static long gPropertiesCacheTS = 0;

    public abstract String getCommandName();

    protected abstract Properties getPropsDefaults();

    public abstract void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    public abstract String getExample();

    public abstract String getDescription();

    protected List<Role> getRolesWhitelist() {
        return Collections.emptyList();
    }

    protected void onInsufficientPermissions(@NotNull MessageReceivedEvent event) { }

    public InteractAdapter() throws RegisterException {
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+") && !commandName.isEmpty())
            throw new RegisterException("Bad command name: " + commandName);
        if (getPropsDefaults() != null)
            writeDefaultProps();
    }

    public Properties getProps() {
        long current = System.currentTimeMillis();
        if (propertiesCache != null && current - propertiesCacheTS <= CACHE_LIFETIME)
            return propertiesCache;

        Properties props = getProps(getCommandName(), getPropsDefaults());
        propertiesCache = props;
        propertiesCacheTS = current;
        return props;
    }

    public void storeProps(Properties properties) {
        storeProps(getCommandName(), properties);
        propertiesCache = properties;
        propertiesCacheTS = System.currentTimeMillis();
    }

    public static void storeGlobalProps(Properties properties) {
        storeProps("global", properties);
        gPropertiesCache = properties;
        gPropertiesCacheTS = System.currentTimeMillis();
    }

    public static Properties getGlobalProps() {
        long current = System.currentTimeMillis();
        if (gPropertiesCache != null && current - gPropertiesCacheTS <= CACHE_LIFETIME)
            return gPropertiesCache;

        Properties def = InteractAdapter.getGlobalPropsDefaults();
        Properties res = getProps("global", def);
        gPropertiesCache = res;
        gPropertiesCacheTS = current;
        return res;
    }

    private static Properties getGlobalPropsDefaults() {
        return new Properties(){{
            setProperty("ROLE_PLAYER", "747533854625235024");
            setProperty("ROLE_GM", "747511188690305115");
            setProperty("CHANNEL_PLAYERS", "765683007046287360");
            setProperty("CHANNEL_STATUS", "766376696974147665");
            setProperty("command_prefix", "//");
        }};
    }

    private static Properties getProps(String name, Properties defaultProps) {
        if (defaultProps == null) defaultProps = new Properties();

        File propsFile = new File(DIR_PROPS, name + ".properties");
        if (!propsFile.exists()) {
            try (FileOutputStream os = new FileOutputStream(propsFile)) {
                defaultProps.store(os, PROPS_COMMENT);
            } catch (IOException e) {
                throw new RuntimeException("Cannot store properties", e);
            }
            return defaultProps;
        }

        try (FileInputStream is = new FileInputStream(propsFile)) {
            Properties result = new Properties(defaultProps);
            result.load(is);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties", e);
        }
    }

    private static void storeProps(String name, Properties properties) {
        File propsFile = new File(DIR_PROPS, name + ".properties");
        try (FileOutputStream os = new FileOutputStream(propsFile)) {
            properties.store(os, PROPS_COMMENT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot store properties", e);
        }
    }

    public static String getPrefix() {
        return getGlobalProps().getProperty("command_prefix");
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event, InteractAdapter[] adapters) {
        if (event.getAuthor().isBot()) return;
        String prefix = getPrefix();
        if (event.getChannelType().isGuild() && !event.getMessage().getContentStripped().startsWith(prefix))
            return;
        InteractAdapter adapter  = null;
        String commandName = "[Unknown]";
        for (InteractAdapter a: adapters) {
            commandName = a.getCommandName();
            if (event.getChannelType().isGuild()) {
                if (!event.getMessage().getContentStripped().startsWith(prefix + commandName))
                    continue;
            } else {
                if (!event.getMessage().getContentStripped().startsWith(commandName))
                    continue;
            }
            adapter = a;
            break;
        }

        LOG.d("Command received: '%s' from user %s", commandName, event.getAuthor().getAsTag());
        if (adapter == null) {
            Messages.printError(event.getChannel(), STR.getString("err.unknown_command.title"), String.format(STR.getString("err.unknown_command"), ZONT_MENTION));
            return;
        }
        if (event.isWebhookMessage()) {
            System.err.println("This is a webhook message, idk how to handle it");
            return;
        }

        List<Role> wl = adapter.getRolesWhitelist();
        if (wl.size() > 0 && event.getMember() == null) {
            Messages.printError(event.getChannel(), STR.getString("err.unknown_perm.title"), STR.getString("err.unknown_perm"));
            return;
        }
        if (wl.size() > 0) {
            boolean permitted = false;
            outer:
            for (Role mr: event.getMember().getRoles()) {
                for (Role ar: wl) {
                    if (mr.getIdLong() == ar.getIdLong()) {
                        permitted = true;
                        break outer;
                    }
                }
            }
            if (!permitted) {
                Messages.printError(event.getChannel(), STR.getString("err.insufficient_perm.title"), STR.getString("err.insufficient_perm"));
                adapter.onInsufficientPermissions(event);
                return;
            }
        }

        try {
            adapter.onRequest(event);
        } catch (UserInvalidArgumentException e) {
            event.getChannel()
                    .sendMessage(Messages.error(
                            STR.getString("err.args.title"),
                            e.getMessage() + (e.printSyntax ? ("\n\n" +
                                    String.format(STR.getString("err.args.syntax"), adapter.getExample())) : "") ))
                    .queue();
        }
    }

    private void writeDefaultProps() {
        String name = getCommandName();
        if (!new File(DIR_PROPS, name + ".properties").exists())
            storeProps(name, getPropsDefaults());
    }

    public static void writeDefaultGlobalProps() {
        if (!new File(DIR_PROPS, "global.properties").exists())
            storeGlobalProps(getGlobalPropsDefaults());
    }

    protected static class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }

    protected static class UserInvalidArgumentException extends RuntimeException {
        boolean printSyntax = true;
        public UserInvalidArgumentException(String s) {
            super(s);
        }
        public UserInvalidArgumentException(String s, boolean printSyntax) {
            super(s);
            this.printSyntax = printSyntax;
        }
    }
}
