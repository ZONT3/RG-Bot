package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.CommandAdapter;
import ru.zont.dsbot.core.Messages;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.rgdsb.listeners.StatusMain;
import ru.zont.rgdsb.tools.Configs;
import ru.zont.rgdsb.tools.Globals;

import java.util.Properties;

import static ru.zont.dsbot.core.Strings.STR;

public class Status extends CommandAdapter {
    public Status(ZDSBot bot) throws RegisterException {
        super(bot);
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
            gm = event.getGuild().getRoleById(Configs.getRoleGmID());
        } catch (Exception ignored) { }
        String gmMention = STR.getString("comm.status.gm_mention_pc");
        if (gm != null)
            gmMention = gm.getAsMention();
        final String finalGmMention = gmMention;

        StatusMain.ServerInfoStruct struct = StatusMain.retrieveServerInfo();
        EmbedBuilder builder = new EmbedBuilder(ru.zont.rgdsb.tools.messages.Status.status(struct, finalGmMention));
        if (Globals.serverStatus != null) {
            Message message = Globals.serverStatus.getMessage(StatusMain.class);
            if (message != null)
                builder.setDescription(String.format("[%s](%s)",
                        STR.getString("comm.status.link_live"), message.getJumpUrl()));
        }
        event.getChannel().sendMessage(Messages.addTimestamp(builder)).queue();
    }

    @Override
    public String getSynopsis() {
        return "status";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.status.desc");
    }
}
