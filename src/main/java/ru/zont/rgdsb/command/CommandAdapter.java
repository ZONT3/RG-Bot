package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.*;

import java.io.File;
import java.util.Properties;

public abstract class CommandAdapter {
    private Properties propertiesCache = null;
    private long propertiesCacheTS = 0;

    public abstract void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    public abstract String getCommandName();

    public abstract String getSynopsis();

    public abstract String getDescription();

    protected abstract Properties getPropsDefaults();

    public boolean checkPermission(MessageReceivedEvent event) { return true; }

    public boolean isHidden() { return false; }

    protected void onInsufficientPermissions(@NotNull MessageReceivedEvent event) {
        Messages.printError(event.getChannel(), Strings.STR.getString("err.insufficient_perm.title"), Strings.STR.getString("err.insufficient_perm"));
    }

    public CommandAdapter() throws RegisterException {
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+") && !commandName.isEmpty())
            throw new RegisterException("Bad command name: " + commandName);
        if (getPropsDefaults() != null)
            writeDefaultProps();
    }

    public Properties getProps() {
        long current = System.currentTimeMillis();
        if (propertiesCache != null && current - propertiesCacheTS <= Configs.CACHE_LIFETIME)
            return propertiesCache;

        Properties props = Configs.getProps(getCommandName(), getPropsDefaults());
        propertiesCache = props;
        propertiesCacheTS = current;
        return props;
    }

    public void storeProps(Properties properties) {
        Configs.storeProps(getCommandName(), properties);
        propertiesCache = properties;
        propertiesCacheTS = System.currentTimeMillis();
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event, CommandAdapter[] adapters) {
        if (event.getAuthor().isBot()) return;
        String prefix = Configs.getPrefix();
        String content = event.getMessage().getContentStripped();
        boolean inGuild = event.getChannelType().isGuild();
        if (inGuild && !content.startsWith(prefix))
            return;
        if (content.startsWith(prefix))
            content = content.substring(prefix.length());
        CommandAdapter adapter  = null;
        String commandName;
        for (CommandAdapter a: adapters) {
            commandName = a.getCommandName();
            if (content.startsWith(commandName)) {
                adapter = a;
                break;
            }
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

        boolean permission = adapter.checkPermission(event) || Configs.isTechAdmin(event.getAuthor().getId());
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
                                    String.format(Strings.STR.getString("err.args.syntax"), adapter.getSynopsis(), inGuild ? prefix : "", adapter.getCommandName())) : "") ))
                    .queue();
        }
    }

    private void writeDefaultProps() {
        String name = getCommandName();
        if (!new File(Configs.DIR_PROPS, name + ".properties").exists())
            Configs.storeProps(name, getPropsDefaults());
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
