package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.Commands;
import ru.zont.rgdsb.InteractAdapter;

import java.awt.*;
import java.util.Map;
import java.util.Properties;

public class Help extends InteractAdapter {
    public Help() throws RegisterException {
        super();
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public Properties getDefaultProps() {
        return null;
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) {
        String inpt = Commands.parseInput(this, event).trim();
        InteractAdapter comm = null;
        boolean b = !inpt.isEmpty();
        if (b) comm = Commands.getCommandByName(inpt);
        if (comm == null) {
            if (b) printError(event.getChannel(), "\u041D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u0430\u044F \u043A\u043E\u043C\u0430\u043D\u0434\u0430", "\u041F\u0435\u0447\u0430\u0442\u0430\u044E \u0442\u0435\u0431\u0435 \u0432\u0435\u0441\u044C \u0441\u043F\u0438\u0441\u043E\u043A");
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("\u0421\u043F\u0438\u0441\u043E\u043A \u043A\u043E\u043C\u0430\u043D\u0434")
                    .setColor(Color.LIGHT_GRAY);
            for (Map.Entry<String, InteractAdapter> e: Commands.getAllCommands().entrySet())
                builder.addField(
                        e.getKey(),
                        String.format("`%s`: %s",
                                e.getValue().getExample(),
                                e.getValue().getDescription().substring(0, Math.min(90, e.getValue().getDescription().length()))
                                        + (e.getValue().getDescription().length() > 90 ? "..." : "")),
                        false );
            event.getChannel().sendMessage(builder.build()).queue();
        } else {
            event.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle(comm.getCommandName())
                            .addField("\u041F\u0440\u0438\u043C\u0435\u0440", String.format("`%s`", comm.getExample()), false)
                            .addField("\u041E\u043F\u0438\u0441\u0430\u043D\u0438\u0435", comm.getDescription(), false)
                            .setColor(Color.LIGHT_GRAY)
                            .build()
            ).queue();
        }
    }

    @Override
    public String getExample() {
        return getPrefix() + "help [command]";
    }

    @Override
    public String getDescription() {
        return "\u041F\u043E\u043B\u0443\u0447\u0438\u0442\u044C \u043F\u043E\u043C\u043E\u0449\u044C (\u043E\u043F\u0438\u0441\u0430\u043D\u0438\u0435) \u043A\u043E\u043C\u0430\u043D\u0434\u044B. \u0415\u0441\u043B\u0438 \u0432\u0432\u0435\u0441\u0442\u0438 \u043F\u0440\u043E\u0441\u0442\u043E help, \u0442\u043E \u0432\u044B\u0439\u0434\u0435\u0442 \u0441\u043F\u0438\u0441\u043E\u043A \u0432\u0441\u0435\u0445 \u043A\u043E\u043C\u0430\u043D\u0434";
    }
}
