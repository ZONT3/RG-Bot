package ru.zont.rgdsb.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.PropertiesTools;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LServerStatus extends ListenerAdapter {
    private List<ServerStatusEntry> entryList;
    private List<Thread> threadList;
    private Map<Class<? extends ServerStatusEntry>, Message> messages;

    private ArrayList<ServerStatusEntry> buildEntryList() {
        return new ArrayList<ServerStatusEntry>() {{
            add(new StatusGMs());
            add(new StatusStatistics());
            add(new StatusMain());
        }};
    }

    public Message getMessage(Class<? extends ServerStatusEntry> key) {
        return messages.get(key);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        TextChannel channel = null;
        String channelStatusID = PropertiesTools.getChannelStatusID();
        for (Guild guild: event.getJDA().getGuilds()) {
            channel = guild.getTextChannelById(channelStatusID);
            if (channel != null) break;
        }
        if (channel == null)
            throw new RuntimeException("Cannot find server state channel with id " + channelStatusID);

        prepare(channel);
        setup(channel);
    }

    private void prepare(TextChannel channel) {
        for (Message message: channel.getHistory().retrievePast(50).complete())
            message.delete().queue();
    }

    private void setup(TextChannel channel) {
        entryList = buildEntryList();
        threadList = new ArrayList<>();
        messages = new HashMap<>();

        for (ServerStatusEntry entry: entryList) {
            Message message = channel.sendMessage(entry.getInitialMsg()).complete();
            messages.put(entry.getClass(), message);

            Thread thread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    entry.update(message);
                    try {
                        Thread.sleep(entry.getPeriod());
                    } catch (InterruptedException interruptedException) {
                        break;
                    }
                }
            });
            thread.start();
            threadList.add(thread);
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        for (Thread thread: threadList) thread.interrupt();
        for (Map.Entry<Class<? extends ServerStatusEntry>, Message> e: messages.entrySet())
            e.getValue().delete().complete();
    }
}
