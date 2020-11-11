package ru.zont.rgdsb;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.interact.InteractAdapter;

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
