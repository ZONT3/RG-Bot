package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.InteractAdapter;

import java.util.Properties;

import static ru.zont.rgdsb.Strings.STR;

public class Ping extends InteractAdapter {
    public Ping() throws RegisterException {
        super();
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
    public String getExample() {
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
