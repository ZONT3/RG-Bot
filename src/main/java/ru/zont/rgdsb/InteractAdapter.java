package ru.zont.rgdsb;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Properties;

public abstract class InteractAdapter {
    // TODO rework this
    private Properties propertiesCache = null;
    private long propertiesCacheTS = 0;

    public abstract String getCommandName();

    protected abstract Properties getPropsDefaults();

    public abstract void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    public abstract String getExample();

    public abstract String getDescription();

    public boolean checkPermission(MessageReceivedEvent event) { return true; }

    protected void onInsufficientPermissions(@NotNull MessageReceivedEvent event) {
        Messages.printError(event.getChannel(), Strings.STR.getString("err.insufficient_perm.title"), Strings.STR.getString("err.insufficient_perm"));
    }

    public InteractAdapter() throws RegisterException {
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+") && !commandName.isEmpty())
            throw new RegisterException("Bad command name: " + commandName);
        if (getPropsDefaults() != null)
            writeDefaultProps();
    }

    public Properties getProps() {
        long current = System.currentTimeMillis();
        if (propertiesCache != null && current - propertiesCacheTS <= PropertiesTools.CACHE_LIFETIME)
            return propertiesCache;

        Properties props = PropertiesTools.getProps(getCommandName(), getPropsDefaults());
        propertiesCache = props;
        propertiesCacheTS = current;
        return props;
    }

    public void storeProps(Properties properties) {
        PropertiesTools.storeProps(getCommandName(), properties);
        propertiesCache = properties;
        propertiesCacheTS = System.currentTimeMillis();
    }

    public static String getPrefix() {
        return PropertiesTools.getGlobalProps().getProperty("command_prefix");
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

        LOG.d("Command received: '%s' from user %s", event.getMessage().getContentRaw(), event.getAuthor().getAsTag());
        if (adapter == null) {
            Messages.printError(event.getChannel(), Strings.STR.getString("err.unknown_command.title"), String.format(Strings.STR.getString("err.unknown_command"), Globals.ZONT_MENTION));
            return;
        }
        if (event.isWebhookMessage()) {
            System.err.println("This is a webhook message, idk how to handle it");
            return;
        }

        boolean permission = adapter.checkPermission(event);
        if (!permission && event.getMember() == null) {
            Messages.printError(event.getChannel(), Strings.STR.getString("err.unknown_perm.title"), Strings.STR.getString("err.unknown_perm"));
            return;
        }
        if (!permission) {
            adapter.onInsufficientPermissions(event);
            return;
        }

        try {
            adapter.onRequest(event);
        } catch (UserInvalidArgumentException e) {
            event.getChannel()
                    .sendMessage(Messages.error(
                            Strings.STR.getString("err.args.title"),
                            e.getMessage() + (e.printSyntax ? ("\n\n" +
                                    String.format(Strings.STR.getString("err.args.syntax"), adapter.getExample())) : "") ))
                    .queue();
        }
    }

    private void writeDefaultProps() {
        String name = getCommandName();
        if (!new File(PropertiesTools.DIR_PROPS, name + ".properties").exists())
            PropertiesTools.storeProps(name, getPropsDefaults());
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
