package ru.zont.rgdsb.tools.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.dsbot.core.tools.Configs;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.rgdsb.tools.Commons;
import ru.zont.rgdsb.tools.GameMasters;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class GMs {
    public static MessageEmbed statusGMsInitial() {
        return new EmbedBuilder()
                .setTitle(STR.getString("comm.gms.get.title"))
                .setDescription(STR.getString("status.gms.retrieving"))
                .build();
    }

    public static MessageEmbed gmList(List<GameMasters.GM> gms, Guild guild) {
        return gmListShort(gms, guild, false, true, true, true);
    }

    public static MessageEmbed gmListShort(List<GameMasters.GM> gms, Guild guild,
                                           boolean s, boolean n, boolean a, boolean o) {
        EmbedBuilder builder = prepareGmList(gms);

        int splitIndex = gms.size() - 8;
        if (splitIndex > 0) {
            int sleep = 0, anger = 0, warn = 0;
            for (GameMasters.GM gm: gms.subList(0, splitIndex)) {
                long lastLogin = gm.lastlogin;
                switch (getEmo((System.currentTimeMillis() - lastLogin) / 1000 / 60 / 60)) {
                    case " :anger:": anger++; break;
                    case " :zzz:": sleep++; break;
                    case " :octagonal_sign:": warn++; break;
                }
            }
            builder.appendDescription(String.format(
                    STR.getString("status.gms.short.info"),
                    Commons.countGMs(splitIndex), sleep, anger, warn, Configs.getPrefix()));
            builder.appendDescription("\n\n");
        }

        for (GameMasters.GM gm: gms.subList(Math.max(0, splitIndex), gms.size()))
            builder.appendDescription(buildField(gm, guild, s, n, a, o));
        return builder.build();
    }

    public static ArrayList<EmbedBuilder> gmList(List<GameMasters.GM> gms, Guild guild,
                                                 boolean s, boolean n, boolean a, boolean o) {
        EmbedBuilder builder = prepareGmList(gms);
        ArrayList<EmbedBuilder> list = new ArrayList<>();
        list.add(builder);

        for (GameMasters.GM gm: gms)
            Messages.appendDescriptionSplit(buildField(gm, guild, s, n, a, o), list);
        return list;
    }

    private static EmbedBuilder prepareGmList(List<GameMasters.GM> gms) {
        EmbedBuilder builder = new EmbedBuilder().setColor(0x9900ff);
        builder.setTitle(STR.getString("comm.gms.get.title"));
        for (GameMasters.GM gm: gms) {
            Date lastLogin = GameMasters.getLastLogin(gm.steamid64);
            gm.lastlogin = lastLogin != null ? lastLogin.getTime() : 0;

            if (gm.armaname == null) gm.armaname = STR.getString("comm.gms.get.unknown");
            else if (gm.armaname.matches("\".+\""))
                gm.armaname = gm.armaname.substring(1, gm.armaname.length() - 1);
        }

        gms.sort(Comparator.comparingLong(ob -> ob.lastlogin));
        return builder;
    }

    private static String buildField(GameMasters.GM gm, Guild guild, boolean s, boolean n, boolean a, boolean o) {
        Member member = guild.getMemberById(gm.userid);
        String memberMention = member != null ?
                member.getAsMention() : STR.getString("comm.gms.get.unknown.person");
        StringBuilder field = new StringBuilder(memberMention).append('\n');

        if (s) field.append(String.format(STR.getString("comm.gms.get.steamid"), gm.steamid64)).append("\n");
        if (n) field.append(String.format(STR.getString("comm.gms.get.armaname"), gm.armaname)).append("\n");
        if (a) field.append(String.format(STR.getString("comm.gms.get.assigned"), getAssigned(gm))).append("\n");
        if (o) field.append(getOnline(gm)).append("\n");

        if (s || n || a || o) field.append('\n');
        return field.toString();
    }

    private static String getAssigned(GameMasters.GM gm) {
        Date assignedDate = GameMasters.getAssignedDate(gm.userid);
        if (assignedDate == null) STR.getString("comm.gms.get.unknown");
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(assignedDate);
    }

    private static String getOnline(GameMasters.GM gm) {
        long lastLogin = gm.lastlogin;
        if (lastLogin == 0) return STR.getString("comm.gms.get.lastlogin.unk");

        long diff = System.currentTimeMillis() - lastLogin;
        long min = diff / 1000 / 60;
        long hr  = min / 60;
        long day = hr / 24;

        String emo = getEmo(hr);

        if (min < 2) return STR.getString("comm.gms.get.lastlogin.n");
        else return String.format(STR.getString("comm.gms.get.lastlogin"), day, hr % 24, min % 60) + emo;
    }

    private static String getEmo(long hr) {
        if (hr > 47) return " :octagonal_sign:";
        else if (hr > 18) return " :anger:";
        else if (hr > 11) return  " :zzz:";
        else return "";
    }
}
