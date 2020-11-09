package ru.zont.rgdsb;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public class LServerState extends ListenerAdapter {
    private ServerInfoWatch siw = null;

    private Message serverStatusMessage = null;
    private Message serverStatisticMessage = null;
    private Role roleGM = null;

    private short playersRecord = -1;
    private int playersTotal = -1;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        for (Guild guild: event.getJDA().getGuilds()) {
            TextChannel channel = guild.getTextChannelById(PropertiesTools.getChannelStatusID());
            Role r = guild.getRoleById(PropertiesTools.getRoleGmID());
            if (channel != null && r != null) {
                prepareServerInfoChannel(channel);
                roleGM = r;
                return;
            }
        }
    }

    public void onServerStateFetch(ServerInfoWatch.ServerInfoStruct struct) {
        playersRecord = struct.record; playersTotal = struct.total;
        if (serverStatusMessage == null) return;
        if (serverStatisticMessage == null) return;
        try {
            serverStatusMessage.editMessage(Messages.status(struct, roleGM.getAsMention())).queue();
            serverStatisticMessage.editMessage(Messages.serverStatistic(playersRecord, playersTotal)).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareServerInfoChannel(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(100).complete();
        if (messages.size() >= 2)
            channel.deleteMessages(messages).queue(__ -> createServerInfoMsgs(channel));
        else if (messages.size() == 1)
            messages.get(0).delete().queue(__ -> createServerInfoMsgs(channel));
        else createServerInfoMsgs(channel);
    }

    private void createServerInfoMsgs(TextChannel channel) {
        channel.sendMessage(Messages.serverStatisticBasic())
                .queue(message -> {
                    serverStatisticMessage = message;
                    channel.sendMessage(Messages.serverInactive())
                            .queue(message1 -> {
                                serverStatusMessage = message1;
                                siw = new ServerInfoWatch(31000);
                                siw.setCallback(this::onServerStateFetch);
                                siw.start();
                            });
                });
    }

    public Message getServerStatusMessage() {
        return serverStatusMessage;
    }

    public Message getServerStatisticMessage() {
        return serverStatisticMessage;
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        siw.interrupt();
        if (serverStatusMessage != null)
            serverStatusMessage.delete().complete();
    }

}
