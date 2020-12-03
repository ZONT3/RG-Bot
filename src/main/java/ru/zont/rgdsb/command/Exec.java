package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.Commands;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Exec extends CommandAdapter {
    public Exec() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String input = Commands.parseInputRaw(this, event);
        Pattern pattern = Pattern.compile("[^\\w]*```(java|python)\\n((.|\\n)+)```[^\\w]*");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find())
            event.getChannel().sendMessage(String.format("1: %s\n2: %s", matcher.group(1), matcher.group(2))).queue();
        else throw new NotImplementedException();
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
