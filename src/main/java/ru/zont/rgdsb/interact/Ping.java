package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.InteractAdapter;

import java.util.Properties;

public class Ping extends InteractAdapter {
    public Ping() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public Properties getDefaultProps() {
        return null;
    }

    @Override
    public String getExample() {
        return getPrefix() + "ping";
    }

    @Override
    public String getDescription() {
        return "\u041F\u043E\u043A\u0430\u0437\u044B\u0432\u0430\u0435\u0442 \u0437\u0430\u0434\u0435\u0440\u0436\u043A\u0443 ~~\u0432 \u0440\u0430\u0437\u0432\u0438\u0442\u0438\u0438~~ \u0440\u0435\u0430\u043A\u0446\u0438\u0438 \u0431\u043E\u0442\u0430";
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
