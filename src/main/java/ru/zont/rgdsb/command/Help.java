package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Globals;
import ru.zont.rgdsb.tools.Messages;
import ru.zont.rgdsb.tools.Configs;

import java.awt.*;
import java.util.Map;
import java.util.Properties;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Help extends CommandAdapter {
    public Help() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public Properties getPropsDefaults() {
        return null;
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) {
        String inpt = Commands.parseInputRaw(this, event);
        CommandAdapter comm = null;
        boolean b = !inpt.isEmpty();
        if (b) comm = Commands.forName(inpt);
        if (comm == null) {
            if (b) Messages.printError(event.getChannel(), STR.getString("comm.help.err.unknown.title"), STR.getString("comm.help.err.unknown"));

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(STR.getString("comm.help.list.title"))
                    .setColor(Color.LIGHT_GRAY);
            for (Map.Entry<String, CommandAdapter> e: Commands.getAllCommands().entrySet()) {
                CommandAdapter command = e.getValue();
                if (command.isHidden()) continue;
                builder.addField(
                        e.getKey(),
                        String.format("`%s%s`: %s",
                                event.getMember() != null ? Configs.getPrefix() : "",
                                getFirstSynopsis(command.getSynopsis()),
                                command.getDescription().substring(0, Math.min(90, command.getDescription().length()))
                                        + (command.getDescription().length() > 90 ? "..." : "")),
                        false);
            }
            builder.setFooter(String.format(STR.getString("version"), Globals.version));
            event.getChannel().sendMessage(builder.build()).queue();
        } else { // Exact command
            event.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle(comm.getCommandName())
                            .addField(STR.getString("comm.help.entry.example"), formatSynopsis(comm.getSynopsis(),
                                    event.getMember() == null ? "" : Configs.getPrefix()), false)
                            .addField(STR.getString("comm.help.entry.desc"), comm.getDescription(), false)
                            .setColor(Color.LIGHT_GRAY)
                            .build()
            ).queue();
        }
    }

    private String getFirstSynopsis(String synopsis) {
        int i = synopsis.indexOf('\n');
        return synopsis.substring(0, i <= 0 ? synopsis.length() : i);
    }

    private String formatSynopsis(String synopsis, String prefix) {
        return String.format("```%s%s```", prefix, synopsis.replaceAll("\n", "\n" + prefix));
    }

    @Override
    public String getSynopsis() {
        return "help [command]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.help.desc");
    }
}
