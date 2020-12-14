package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.commands.Commands;

import java.io.File;
import java.util.Properties;

import static ru.zont.dsbot.core.commands.Commands.Input;
import static ru.zont.dsbot.core.commands.Commands.parseInput;
import static ru.zont.dsbot.core.tools.Strings.STR;

public class Do extends CommandAdapter {
    public Do(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        final Input input = parseInput(this, event);
        final String raw = input.getRaw();
        if (!raw.matches("[\\w\\-. ]+"))
            throw new UserInvalidArgumentException(STR.getString("comm.do.err.name"));

        Commands.call(Exec.class,
                String.format("-V \"--name=%s\" python -X utf8 -u %s", raw, resolveMain(raw.toLowerCase())),
                event, getBot());
    }

    private String resolveMain(String raw) {
        File dir = new File("scripts", raw);
        if (!dir.exists()) throw new UserInvalidArgumentException(STR.getString("comm.do.err.name"));
        File main = new File(dir, "main.py");
        if (!main.exists()) throw new UserInvalidArgumentException(STR.getString("comm.do.err.name"));
        return main.getAbsolutePath();
    }

    @Override
    public String getCommandName() {
        return "do";
    }

    @Override
    public String getSynopsis() {
        return "do <script_name>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.do.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }
}
