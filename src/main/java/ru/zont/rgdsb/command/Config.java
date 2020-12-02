package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Configs;
import ru.zont.rgdsb.tools.LOG;
import ru.zont.rgdsb.tools.Messages;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Config extends CommandAdapter {
    public Config() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String[] args = Commands.parseArgs(this, event);
        if (args.length == 0)
            throw new UserInvalidArgumentException(STR.getString("err.incorrect_args"));
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 4)
                    throw new UserInvalidArgumentException(STR.getString("err.incorrect_args"));
                if (args.length > 4)
                    for (int i = 4; i < args.length; i++)
                        args[3] += (" " + args[i]);
                set(args[1], args[2], args[3]);
                event.getMessage().addReaction("\u2705").queue();
                break;
            case "get":
                event.getChannel().sendMessage(get(args)).queue();
                break;
            default:
                throw new UserInvalidArgumentException(String.format(
                        STR.getString("comm.config.err.incorrect_mode"), args[0].toLowerCase() ));
        }
    }

    private MessageEmbed get(String[] args) {
        if (args.length == 1) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(STR.getString("comm.config.get.all"));

            builder.addField(
                    STR.getString("comm.config.get.global"),
                    parseProps(Configs.getGlobalProps()),
                    false );

            HashMap<String, CommandAdapter> comms = Commands.getAllCommands();
            for (Map.Entry<String, CommandAdapter> entry: comms.entrySet())
                if (entry.getValue().getProps().size() > 0)
                    builder.addField( entry.getKey(), parseProps(entry.getValue().getProps()), false );

            return builder.build();
        } else if (args.length == 2) {
            CommandAdapter comm = Commands.forName(args[1]);
            if (comm == null && !args[1].toLowerCase().equals("global"))
                throwUnknownComm(args[1]);

            String commandName = comm != null ? comm.getCommandName() : STR.getString("comm.config.get.global");
            Properties props = comm != null ? comm.getProps() : Configs.getGlobalProps();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(commandName);
            builder.setDescription(parseProps(props));
            return builder.build();
        } else if (args.length >= 3) {
            CommandAdapter comm = Commands.forName(args[1]);
            if (comm == null && !args[1].toLowerCase().equals("global"))
                throwUnknownComm(args[1]);

            String commandName = comm != null ? comm.getCommandName() : STR.getString("comm.config.get.global");
            Properties props = comm != null ? comm.getProps() : Configs.getGlobalProps();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(commandName + "." + args[2]);
            String property = props.getProperty(args[2]);
            builder.setDescription(property == null ? "`null`" : property);
            if (property == null)
                builder.setColor(Color.RED);
            return builder.build();
        }
        return Messages.error("Unknown error", "WTFerror");
    }

    private String parseProps(Properties props) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry: props.entrySet())
            sb.append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue())
                    .append('\n');
        return sb.toString();
    }

    private void set(String command, String key, String value) {
        if (command.toLowerCase().equals("global")) {
            LOG.d("Modifying global config: k=%s v=%s", key, value);

            Properties props = Configs.getGlobalProps();
            props.setProperty(key, value);
            Configs.storeGlobalProps(props);
        } else {
            CommandAdapter comm = Commands.forName(command);
            if (comm == null)
                throwUnknownComm(command);

            LOG.d("Modifying config of %s: k=%s v=%s", command, key, value);

            Properties props = comm.getProps();
            props.setProperty(key, value);
            comm.storeProps(props);
        }

    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        ArrayList<String> args = Commands.parseInput(this, event).getArgs();
        String arg1 = args.size() > 0 ? args.get(0) : null;
        String arg3 = args.size() > 2 ? args.get(2) : null;
        if ("set".equalsIgnoreCase(arg1) && "TA_IDS".equalsIgnoreCase(arg3)) return false;
        return member.hasPermission(Permission.ADMINISTRATOR);
    }

    private void throwUnknownComm(String command) {
        throw new UserInvalidArgumentException(String.format(STR.getString("comm.config.err.unknown_comm"), command), false);
    }

    @Override
    public String getCommandName() {
        return "config";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getSynopsis() {
        return "config get|set [command [key [value]]]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.config.desc");
    }
}
