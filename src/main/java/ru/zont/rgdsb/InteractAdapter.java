package ru.zont.rgdsb;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.usrlib.material.MaterialColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public abstract class InteractAdapter extends ListenerAdapter {
    private static final String PROPS_COMMENT = "Properties of Right Games project's DS Bot Command";

    protected abstract String getCommandName();

    protected abstract Properties getDefaultProps();

    protected abstract void onRequest(@NotNull MessageReceivedEvent event);

    protected List<Role> getRolesWhitelist() {
        return Collections.emptyList();
    }

    protected void onInsufficientPermissions(@NotNull MessageReceivedEvent event) { }

    protected InteractAdapter() throws RegisterException {
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+"))
            throw new RegisterException("Bad command name: " + commandName);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String prefix = getGlobalProps().getProperty("command_prefix");
        String commandName = getCommandName();
        if (event.getChannelType().isGuild()) {
            if (!event.getMessage().getContentStripped().startsWith(prefix + commandName))
                return;
        } else {
            if (!event.getMessage().getContentStripped().startsWith(commandName))
                return;
        }

        System.out.printf("Command received: '%s' from user %s\n", commandName, event.getAuthor().getAsTag());
        if (event.isWebhookMessage()) {
            System.err.println("This is a webhook message, idk how to handle it");
            return;
        }

        List<Role> wl = getRolesWhitelist();
        if (wl.size() > 0 && event.getMember() == null) {
            printError(event.getChannel(), "\u041D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u044B \u043F\u0440\u0430\u0432\u0430", "\u042F \u043F\u043E\u043A\u0430 \u0447\u0442\u043E \u043D\u0435 \u043C\u043E\u0433\u0443 \u0443\u0437\u043D\u0430\u0442\u044C, \u0435\u0441\u0442\u044C \u043B\u0438 \u0443 \u0442\u0435\u0431\u044F \u043F\u0440\u0430\u0432\u0430 \u043D\u0430 \u0432\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0438\u0435 \u044D\u0442\u043E\u0439 \u043A\u043E\u043C\u0430\u043D\u0434\u044B, \u043A\u043E\u0433\u0434\u0430 \u0442\u044B \u043F\u0438\u0448\u0435\u0448\u044C \u043C\u043D\u0435 \u0432 \u043B\u0438\u0447\u043A\u0443. \u0415\u0441\u043B\u0438 \u043E\u043D\u0438 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0442\u0435\u043B\u044C\u043D\u043E \u0435\u0441\u0442\u044C, \u043F\u0438\u0448\u0438 \u043C\u043D\u0435 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435!");
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
                printError(event.getChannel(), "\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u043F\u0440\u0430\u0432", "\u0423 \u0442\u0435\u0431\u044F \u043D\u0435\u0442 \u043F\u0440\u0430\u0432 \u043D\u0430 \u0432\u044B\u043F\u043E\u043B\u043D\u0435\u043D\u0438\u0435 \u044D\u0442\u043E\u0439 \u043A\u043E\u043C\u0430\u043D\u0434\u044B");
                onInsufficientPermissions(event);
                return;
            }
        }
        onRequest(event);
    }

    private Properties getProps() {
        return getProps(getCommandName(), getDefaultProps());
    }

    private void storeProps(Properties properties) {
        storeProps(getCommandName(), properties);
    }

    private static Properties getGlobalProps() {
        Properties def = new Properties();
        def.setProperty("command_prefix", "//");
        return getProps("global", def);
    }

    private static Properties getProps(String name, Properties defaultProps) {
        if (defaultProps == null) defaultProps = new Properties();

        File propsFile = new File(Main.DIR_PROPS, name + ".properties");
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
        File propsFile = new File(Main.DIR_PROPS, name + ".properties");
        try (FileOutputStream os = new FileOutputStream(propsFile)) {
            properties.store(os, PROPS_COMMENT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot store properties", e);
        }
    }

    private static void printError(MessageChannel channel, String title, String description) {
        channel.sendMessage(
                new EmbedBuilder()
                        .setTitle(title)
                        .setDescription(description)
                        .setColor(MaterialColor.RED.getValue())
                        .build()).queue();
    }

    protected static class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }
}
