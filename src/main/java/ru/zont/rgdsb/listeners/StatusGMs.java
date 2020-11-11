package ru.zont.rgdsb.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.rgdsb.GameMasters;
import ru.zont.rgdsb.Messages;

public class StatusGMs extends ServerStatusEntry {
    @Override
    MessageEmbed getInitialMsg() {
        return Messages.statusGMsInitial();
    }

    @Override
    void update(Message entryMessage) {
        entryMessage.editMessage(GameMasters.retrieveGmsEmbed(entryMessage.getGuild())).queue();
    }
}
