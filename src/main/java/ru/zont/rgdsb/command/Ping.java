package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.ZDSBot;

import java.util.Properties;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class Ping extends CommandAdapter {
    public Ping(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public Properties getPropsDefaults() {
        return null;
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return true;
    }

    @Override
    public String getSynopsis() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.ping.desc");
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) {
        Message origin = event.getMessage();
        long found = System.currentTimeMillis();
        event.getChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("Pong!")
                        .build()
        ).queue(message -> message.editMessage(
                new EmbedBuilder()
                        .setTitle("Pong!")
                        .addField("Messages diff",
                                String.format("%d ms", message.getTimeCreated().toInstant().toEpochMilli() - origin.getTimeCreated().toInstant().toEpochMilli()),
                                false)
                        .addField("Server delay",
                                String.format("%d ms", System.currentTimeMillis() - found),
                                false)
                        .build()
        ).queue());
    }
}
