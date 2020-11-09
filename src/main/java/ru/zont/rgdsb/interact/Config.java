package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.Commands;
import ru.zont.rgdsb.InteractAdapter;
import ru.zont.rgdsb.LOG;
import ru.zont.rgdsb.Messages;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ru.zont.rgdsb.Main.STR;

public class Config extends InteractAdapter {
    public Config() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String[] args = Commands.parseArgs(this, event);
        if (args.length == 0)
            throw new UserInvalidArgumentException(STR.getString("comm.config.err.incargs"));
        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length < 4)
                    throw new UserInvalidArgumentException(STR.getString("comm.config.err.incargs"));
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
                        STR.getString("comm.config.err.incmode"), args[0].toLowerCase() ));
        }
    }

    private MessageEmbed get(String[] args) {
        if (args.length == 1) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(STR.getString("comm.config.get.all"));

            builder.addField(
                    STR.getString("comm.config.get.global"),
                    parseProps(getGlobalProps()),
                    false );

            HashMap<String, InteractAdapter> comms = Commands.getAllCommands();
            for (Map.Entry<String, InteractAdapter> entry: comms.entrySet())
                if (entry.getValue().getProps().size() > 0)
                    builder.addField( entry.getKey(), parseProps(entry.getValue().getProps()), false );

            return builder.build();
        } else if (args.length == 2) {
            InteractAdapter comm = Commands.forName(args[1]);
            if (comm == null && !args[1].toLowerCase().equals("global"))
                throwUnknownComm(args[1]);

            String commandName = comm != null ? comm.getCommandName() : STR.getString("comm.config.get.global");
            Properties props = comm != null ? comm.getProps() : getGlobalProps();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(commandName);
            builder.setDescription(parseProps(props));
            return builder.build();
        } else if (args.length >= 3) {
            InteractAdapter comm = Commands.forName(args[1]);
            if (comm == null && !args[1].toLowerCase().equals("global"))
                throwUnknownComm(args[1]);

            String commandName = comm != null ? comm.getCommandName() : STR.getString("comm.config.get.global");
            Properties props = comm != null ? comm.getProps() : getGlobalProps();

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

            Properties props = InteractAdapter.getGlobalProps();
            props.setProperty(key, value);
            InteractAdapter.storeGlobalProps(props);
        } else {
            InteractAdapter comm = Commands.forName(command);
            if (comm == null)
                throwUnknownComm(command);

            LOG.d("Modifying config of %s: k=%s v=%s", command, key, value);

            Properties props = comm.getProps();
            props.setProperty(key, value);
            comm.storeProps(props);
        }

    }

    private void throwUnknownComm(String command) {
        throw new UserInvalidArgumentException(String.format(STR.getString("comm.config.err.unkncomm"), command), false);
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
    public String getExample() {
        return "config (get|set) [command [key [value]]]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.config.desc");
    }
}
