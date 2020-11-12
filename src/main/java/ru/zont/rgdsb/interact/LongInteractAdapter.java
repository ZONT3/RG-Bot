package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public abstract class LongInteractAdapter extends InteractAdapter {
    public LongInteractAdapter() throws RegisterException {
        super();
    }

    public abstract void onRequestLong(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        event.getChannel().sendTyping().complete();
        onRequestLong(event);
    }
}
