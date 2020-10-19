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

    protected abstract Properties getDefaultProps();

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
    }

    private Properties getProps() {
        long current = System.currentTimeMillis();
        if (propertiesCache != null && current - propertiesCacheTS <= CACHE_LIFETIME)
            return propertiesCache;

        Properties props = getProps(getCommandName(), getDefaultProps());
        propertiesCache = props;
        propertiesCacheTS = current;
        return props;
    }

    private void storeProps(Properties properties) {
        storeProps(getCommandName(), properties);
        propertiesCache = properties;
        propertiesCacheTS = System.currentTimeMillis();
    }

    private static Properties getGlobalProps() {
        long current = System.currentTimeMillis();
        if (gPropertiesCache != null && current - gPropertiesCacheTS <= CACHE_LIFETIME)
            return gPropertiesCache;

        Properties def = new Properties();
        def.setProperty("command_prefix", "//");
        Properties res = getProps("global", def);
        gPropertiesCache = res;
        gPropertiesCacheTS = current;
        return res;
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

        System.out.printf("Command received: '%s' from user %s\n", commandName, event.getAuthor().getAsTag());
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

        adapter.onRequest(event);
    }

    protected static class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }

    protected static class UserInvalidArgumentException extends RuntimeException {
        public UserInvalidArgumentException(String s) {
            super(s);
        }
    }
}
