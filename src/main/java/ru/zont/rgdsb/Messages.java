package ru.zont.rgdsb;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.LocalTime;
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

    public static MessageEmbed status(ServerInfoWatch.ServerInfoStruct serverInfo, String gmMention) {
        long time = serverInfo.time;
        short online = serverInfo.count;
        short playersRecord = serverInfo.record;
        long restart = serverInfo.uptime;
        List<String> gms = serverInfo.gms;
        for (int i = 0; i < gms.size(); i++)
            gms.set(i, trimNick(gms.get(i)));
        if (time >= 45000) return serverInactive();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(STR.getString("servstate.title"))
                .addField(STR.getString("servstate.online"), Strings.countPlayers(online), true)
                .addField(
                        STR.getString("servstate.restart"),
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
        builder.addField(STR.getString("servstate.gms"), sb.toString(), false);

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
            return String.format(STR.getString("servstate.gms.absent.day"), gm);
        } else {
            return STR.getString("servstate.gms.absent.night");
        }
    }

    public static MessageEmbed serverInactive() {
        return new EmbedBuilder()
                .setTitle(STR.getString("servstate.title"))
                .setDescription(STR.getString("servstate.inactive"))
                .build();
    }

    public static MessageEmbed serverStatisticBasic() {
        return new EmbedBuilder()
                .setTitle(STR.getString("servstats.title"))
                .setDescription(STR.getString("servstats.connect"))
                .build();
    }

    public static MessageEmbed serverStatistic(short playersRecord, int playersTotal) {
        if (playersRecord <= 0) return serverStatisticBasic();
        if (playersTotal <= 0) return serverStatisticBasic();
        return new EmbedBuilder()
                .setColor(Color.lightGray)
                .setTitle(STR.getString("servstats.title"))
                .addField(STR.getString("servstats.record"), Strings.countPlayers(playersRecord), false)
                .addField(STR.getString("servstats.total"), Strings.countPlayers(playersTotal), false)
                .build();
    }

    private static String trimNick(String nick) {
        return nick.replaceAll("[\"']", "").trim()
                .replaceAll("\\[.+] *", "")
                .replaceAll("[ .]+.\\...?", "");
    }
}
