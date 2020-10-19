package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.Commands;
import ru.zont.rgdsb.InteractAdapter;
import ru.zont.rgdsb.Messages;

import java.awt.*;
import java.util.Map;
import java.util.Properties;

import static ru.zont.rgdsb.Main.STR;

public class Help extends InteractAdapter {
    public Help() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public Properties getDefaultProps() {
        return null;
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) {
        String inpt = Commands.parseInput(this, event).trim();
        InteractAdapter comm = null;
        boolean b = !inpt.isEmpty();
        if (b) comm = Commands.getCommandByName(inpt);
        if (comm == null) {
            if (b) Messages.printError(event.getChannel(), STR.getString("comm.help.err.unknown.title"), STR.getString("comm.help.err.unknown"));
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(STR.getString("comm.help.list.title"))
                    .setColor(Color.LIGHT_GRAY);
            for (Map.Entry<String, InteractAdapter> e: Commands.getAllCommands().entrySet())
                builder.addField(
                        e.getKey(),
                        String.format("`%s`: %s",
                                e.getValue().getExample(),
                                e.getValue().getDescription().substring(0, Math.min(90, e.getValue().getDescription().length()))
                                        + (e.getValue().getDescription().length() > 90 ? "..." : "")),
                        false );
            event.getChannel().sendMessage(builder.build()).queue();
        } else {
            event.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle(comm.getCommandName())
                            .addField(STR.getString("comm.help.entry.example"), String.format("`%s`", comm.getExample()), false)
                            .addField(STR.getString("comm.help.entry.desc"), comm.getDescription(), false)
                            .setColor(Color.LIGHT_GRAY)
                            .build()
            ).queue();
        }
    }

    @Override
    public String getExample() {
        return getPrefix() + "help [command]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.help.desc");
    }
}
