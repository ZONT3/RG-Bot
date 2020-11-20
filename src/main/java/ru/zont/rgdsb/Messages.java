package ru.zont.rgdsb;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.rgdsb.listeners.StatusMain;

import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static ru.zont.rgdsb.Strings.STR;

public class Messages {
    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED)
                .build();
    }

    public static void printError(MessageChannel channel, String title, String description) {
        channel.sendMessage(error(title, description)).queue();
    }

    public static MessageEmbed status(StatusMain.ServerInfoStruct serverInfo, String gmMention) {
        long time = serverInfo.time;
        short online = serverInfo.count;
        short playersRecord = serverInfo.record;
        long restart = serverInfo.uptime;
        List<String> gms = serverInfo.gms;
        for (int i = 0; i < gms.size(); i++)
            gms.set(i, trimNick(gms.get(i)));
        if (time >= 45000) return serverInactive();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(STR.getString("status.main.title"))
                .addField(STR.getString("status.main.online"), Strings.countPlayers(online), true)
                .addField(
                        STR.getString("status.main.restart"),
                        String.format( STR.getString("time.hm"),
                                restart / 60 / 60,
                                restart / 60 % 60 ),
                        true);

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (String gm: gms) {
            String app = (sb1.length() > 0 ? ", " : "") + gm;
            if (sb1.length() == 0 || sb1.length() + app.length() <= 26)
                sb1.append(app);
            else {
                sb.append(String.format(":zap: %26s\n", sb1.toString()));
                sb1 = new StringBuilder();
            }
        }
        if (sb1.length() > 0)
            sb.append(String.format(":zap: %26s\n", sb1.toString()));
        if (sb.length() == 0)
            sb.append(noGmString(gmMention));
        builder.addField(STR.getString("status.main.gms"), sb.toString(), false);

        if (playersRecord >= 0) {
            int g = 255 * online / playersRecord;
            if (g > 255) g = 255;
            int r = 255 - g;
            builder.setColor(new Color(r, g, 0));
        }

        return builder.build();
    }

    private static String noGmString(String gm) {
        int h = LocalTime.now().getHour();
        if (h >= 15 && h <= 22) {
            return String.format(STR.getString("status.main.gms.absent.day"), gm);
        } else {
            return STR.getString("status.main.gms.absent.night");
        }
    }

    public static MessageEmbed serverInactive() {
        return new EmbedBuilder()
                .setTitle(STR.getString("status.main.title"))
                .setDescription(STR.getString("status.main.inactive"))
                .build();
    }

    public static MessageEmbed serverStatisticInitial() {
        return new EmbedBuilder()
                .setTitle(STR.getString("status.statistics.title"))
                .setDescription(STR.getString("status.statistics.connect"))
                .build();
    }

    public static MessageEmbed serverStatistic(short playersRecord, int playersTotal) {
        if (playersRecord <= 0) return serverStatisticInitial();
        if (playersTotal <= 0) return serverStatisticInitial();
        return new EmbedBuilder()
                .setColor(Color.lightGray)
                .setTitle(STR.getString("status.statistics.title"))
                .addField(STR.getString("status.statistics.record"), Strings.countPlayers(playersRecord), false)
                .addField(STR.getString("status.statistics.total"), Strings.countPlayers(playersTotal), false)
                .build();
    }
    
    public static MessageEmbed statusGMsInitial() {
        return new EmbedBuilder()
                .setTitle(STR.getString("comm.gms.get.title"))
                .setDescription(STR.getString("status.gms.retrieving"))
                .build();
    }

    private static String trimNick(String nick) {
        return nick.replaceAll("[\"']", "").trim()
                .replaceAll("\\[.+] *", "")
                .replaceAll("[ .]+.\\...?", "");
    }

    public static MessageEmbed addTimestamp(MessageEmbed e) {
        return addTimestamp(new EmbedBuilder(e));
    }

    public static MessageEmbed addTimestamp(EmbedBuilder builder) {
        return builder.setTimestamp(Instant.now()).build();
    }

    public static MessageEmbed gmList(List<GameMasters.GM> gms, Guild guild) {
        return gmList(gms, guild, false, true, true, true);
    }

    public static MessageEmbed gmList(List<GameMasters.GM> gms, Guild guild,
                                      boolean s, boolean n, boolean a, boolean o) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(STR.getString("comm.gms.get.title"));
        for (GameMasters.GM gm: gms) {
            Date lastLogin = GameMasters.getLastLogin(gm.steamid64);
            gm.lastlogin = lastLogin != null ? lastLogin.getTime() : 0;

            if (gm.armaname == null) gm.armaname = STR.getString("comm.gms.get.unknown");
            else if (gm.armaname.matches("\".+\""))
                gm.armaname = gm.armaname.substring(1, gm.armaname.length() - 1);
        }

        gms.sort(Comparator.comparingLong(ob -> ob.lastlogin));

        for (GameMasters.GM gm: gms) {
            Member member = guild.getMemberById(gm.userid);
            String memberMention = member != null ?
                    member.getAsMention() : STR.getString("comm.gms.get.unknown.person");
            StringBuilder field = new StringBuilder(memberMention).append('\n');

            if (s) field.append(String.format(STR.getString("comm.gms.get.steamid"), gm.steamid64)).append("\n");
            if (n) field.append(String.format(STR.getString("comm.gms.get.armaname"), gm.armaname)).append("\n");
            if (a) field.append(String.format(STR.getString("comm.gms.get.assigned"), getAssigned(gm))).append("\n");
            if (o) field.append(getOnline(gm)).append("\n");

            if (s || n || a || o) field.append('\n');
            builder.appendDescription(field.toString());
        }
        return builder.setColor(0x9900ff).build();
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

        String emo;
        if (hr > 18) emo = " :anger:";
        else if (hr > 11) emo = " :zzz:";
        else emo = "";

        if (min < 2) return STR.getString("comm.gms.get.lastlogin.n");
        else return String.format(STR.getString("comm.gms.get.lastlogin"), day, hr % 24, min % 60) + emo;
    }
}
