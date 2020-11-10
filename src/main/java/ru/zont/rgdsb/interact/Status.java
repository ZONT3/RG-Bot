package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.*;

import java.time.Instant;
import java.util.Properties;

import static ru.zont.rgdsb.Strings.STR;

public class Status extends InteractAdapter {
    public Status() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "status";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Role gm = null;
        try {
            gm = event.getGuild().getRoleById(PropertiesTools.getRoleGmID());
        } catch (Exception ignored) { }
        String gmMention = STR.getString("comm.status.gm_mention_pc");
        if (gm != null)
            gmMention = gm.getAsMention();
        final String finalGmMention = gmMention;

        ServerInfoWatch watch = new ServerInfoWatch();
        watch.setCallback(struct -> {
            MessageEmbed e = Messages.status(struct, finalGmMention);
            EmbedBuilder builder = new EmbedBuilder(e)
                    .setTimestamp(Instant.now());
            if (Globals.serverState != null && Globals.serverState.getServerStatusMessage() != null)
                builder.setDescription(String.format("%s:\n%s", STR.getString("comm.status.link_live"), Globals.serverState.getServerStatusMessage().getJumpUrl()));
            e = builder.build();
            event.getChannel().sendMessage(e).queue();
        });
        watch.start();
    }

    @Override
    public String getExample() {
        return "status";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.status.desc");
    }
}
