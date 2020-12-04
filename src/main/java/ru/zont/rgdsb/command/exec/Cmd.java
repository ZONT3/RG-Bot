package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.NotImplementedException;
import ru.zont.rgdsb.command.CommandAdapter;

import java.util.Properties;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Cmd extends CommandAdapter {
    public Cmd() throws RegisterException { super(); }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        throw new NotImplementedException();
        // TODO систему наследования
    }

    @Override
    public String getCommandName() {
        return "cmd";
    }

    @Override
    public String getSynopsis() {
        return "cmd <command>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.cmd.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }
}
