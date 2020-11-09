package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.InteractAdapter;

import java.util.Properties;

import static ru.zont.rgdsb.Main.STR;

public class Config extends InteractAdapter {
    public Config() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        
    }

    @Override
    public String getCommandName() {
        return "config";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getExample() {
        return "config (get|set) [command [key [value]]]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.config.desc");
    }
}
