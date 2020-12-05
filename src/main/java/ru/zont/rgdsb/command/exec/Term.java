package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Messages;
import ru.zont.rgdsb.tools.Strings;

import java.util.Properties;

public class Term extends CommandAdapter {

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String input = Commands.parseRaw(this, event);
        if (!input.matches("\\d+")) throw new UserInvalidArgumentException("PID **only** required");

        MessageChannel channel = event.getChannel();

        ExecHandler h = Exec.findProcess(Long.parseLong(input));
        if (h == null) {
            channel.sendMessage(new EmbedBuilder()
                    .setColor(0xAC3311)
                    .setTitle("No such process")
                    .build()).queue();
            return;
        }

        h.terminate();
        Messages.addOK(event.getMessage());
    }

    @Override
    public String getCommandName() {
        return "term";
    }

    @Override
    public String getSynopsis() {
        return "term <pid>";
    }

    @Override
    public String getDescription() {
        return Strings.STR.getString("comm.term.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }
}
