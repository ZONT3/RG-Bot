package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.Commands;

import java.util.Properties;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Exec extends CommandAdapter {
    public Exec() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        SubprocessListener listener =
                new SubprocessListener(
                        "cmd /c " + Commands.parseInputRaw(this, event),
                        event.getChannel());
        listener.start();
    }

    @Override
    public String getCommandName() {
        return "exec";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getSynopsis() {
        return "exec <input>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.cmd.desc");
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return false;
    }
}
