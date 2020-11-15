package ru.zont.rgdsb.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.rgdsb.GameMasters;
import ru.zont.rgdsb.Messages;

import java.util.ArrayList;
import java.util.List;

import static ru.zont.rgdsb.Strings.STR;

public class StatusGMs extends ServerStatusEntry {
    @Override
    MessageEmbed getInitialMsg() {
        return Messages.statusGMsInitial();
    }

    @Override
    void update(Message entryMessage) {
        ArrayList<GameMasters.GM> gms = GameMasters.retrieve();
        updateNicks(gms, entryMessage.getGuild());
        entryMessage.editMessage(Messages.gmList(gms, entryMessage.getGuild())).queue();
    }

    private void updateNicks(List<GameMasters.GM> gms, Guild guild) {
        for (GameMasters.GM gm: gms) {
            Member member = guild.getMemberById(gm.userid);
            String armaName = GameMasters.getArmaName(gm.steamid64);
            String dsname = member != null ? member.getEffectiveName() : STR.getString("comm.gms.get.unknown");
            if (!gm.dsname.equals(dsname) || !gm.armaname.equals(armaName)) {
                gm.armaname = armaName;
                gm.dsname = dsname;
                GameMasters.setGm(gm);
            }
        }
    }
}