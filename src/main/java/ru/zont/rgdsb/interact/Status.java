package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.InteractAdapter;
import ru.zont.rgdsb.LServerState;
import ru.zont.rgdsb.ServerInfoWatch;

import java.time.Instant;
import java.util.Properties;

public class Status extends InteractAdapter {
    public Status() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "status";
    }

    @Override
    protected Properties getDefaultProps() {
        return null;
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Role gm = event.getGuild().getRoleById(747511188690305115L);
        if (gm == null) {
            printError(event.getChannel(), "\u041E\u0448\u0438\u0431\u043A\u0430", "\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u043D\u0430\u0439\u0442\u0438 \u0440\u043E\u043B\u044C \u0413\u041C\u043E\u0432");
            return;
        }

        ServerInfoWatch watch = new ServerInfoWatch();
        watch.setCallback((count, time, uptime, gms, record, total) -> {
            MessageEmbed e = LServerState.formatStatusMsg(count, uptime, LServerState.getGmList(gms), time, record, gm.getAsMention());
            e = new EmbedBuilder(e).setTimestamp(Instant.now()).build();
            event.getChannel().sendMessage(e).queue();
        });
        watch.start();
    }

    @Override
    public String getExample() {
        return getPrefix() + "status";
    }

    @Override
    public String getDescription() {
        return "\u041F\u043E\u043B\u0443\u0447\u0438\u0442\u044C \u0441\u0442\u0430\u0442\u0443\u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u043D\u0430 \u0442\u0435\u043A\u0443\u0449\u0438\u0439 \u043C\u043E\u043C\u0435\u043D\u0442";
    }
}
