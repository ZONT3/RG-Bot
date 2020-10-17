package ru.zont.rgdsb;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;

import static ru.zont.rgdsb.Main.CHANNEL_STATUS;

public class LServerState extends ListenerAdapter {
    private ServerInfoWatch siw = null;

    private Message serverStatusMessage = null;
    private Message serverStatisticMessage = null;
    private Role roleGM = null;

    private short playersRecord = -1;
    private int playersTotal = -1;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        for (Guild guild: event.getJDA().getGuilds()) {
            TextChannel channel = guild.getTextChannelById(CHANNEL_STATUS);
            Role r = guild.getRoleById(747511188690305115L);
            if (channel != null && r != null) {
                prepareServerInfoChannel(channel);
                roleGM = r;
                return;
            }
        }
    }

    public void onServerStateFetch(short count, long time, long uptime, String gms, short rec, int total) {
        playersRecord = rec; playersTotal = total;
        if (serverStatusMessage == null) return;
        if (serverStatisticMessage == null) return;
        try {
            List<String> gmList = getGmList(gms);
            serverStatusMessage.editMessage(formatStatusMsg(count, uptime, gmList, time, playersRecord, roleGM.getAsMention())).queue();
            serverStatisticMessage.editMessage(serverStatistic()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getGmList(String gms) {
        return new Gson().fromJson(gms, new TypeToken<List<String>>() {}.getType());
    }

    private void prepareServerInfoChannel(TextChannel channel) {
        List<Message> messages = channel.getHistory().retrievePast(100).complete();
        if (messages.size() >= 2)
            channel.deleteMessages(messages).queue(__ -> createServerInfoMsgs(channel));
        else if (messages.size() == 1)
            messages.get(0).delete().queue(__ -> createServerInfoMsgs(channel));
        else createServerInfoMsgs(channel);
    }

    private void createServerInfoMsgs(TextChannel channel) {
        channel.sendMessage(serverStatisticBasic())
                .queue(message -> {
                    serverStatisticMessage = message;
                    channel.sendMessage(serverInactive())
                            .queue(message1 -> {
                                serverStatusMessage = message1;
                                siw = new ServerInfoWatch(31000);
                                siw.setCallback(this::onServerStateFetch);
                                siw.start();
                            });
                });
    }

    private static String trimNick(String nick) {
        return nick.replaceAll("[\"']", "").trim()
                .replaceAll("\\[.+] *", "")
                .replaceAll("[ .]+.\\...?", "");
    }


    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        siw.interrupt();
        if (serverStatusMessage != null)
            serverStatusMessage.delete().complete();
    }

    public static MessageEmbed formatStatusMsg(short online, long restart, List<String> gms, long time, short playersRecord, String gmm) {
        if (time >= 45000) return serverInactive();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("**\u0421\u0442\u0430\u0442\u0443\u0441 \u0421\u0435\u0440\u0432\u0435\u0440\u0430**\n")
                .addField(":green_circle: \u041e\u043d\u043b\u0430\u0439\u043d", countPlayers(online), true)
                .addField(
                        ":timer: \u041e\u0442 \u0440\u0435\u0441\u0442\u0430\u0440\u0442\u0430",
                        String.format( "%02d\u0447 %02d\u043c\u0438\u043d\n",
                                restart / 60 / 60,
                                restart / 60 % 60 ),
                        true);

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (String gm: gms) {
            String app = (sb1.length() > 0 ? ", " : "") + trimNick(gm);
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
            sb.append(noGmString(gmm));
        builder.addField(":crossed_swords: \u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u0413\u041c\u044b", sb.toString(), false);

        if (playersRecord >= 0) {
            int g = 255 * online / playersRecord;
            if (g > 255) g = 255;
            int r = 255 - g;
            builder.setColor(new Color(r, g, 0));
        }

        return builder.build();
//        StringBuilder sb = new StringBuilder();
//        sb.append("**\u0421\u0442\u0430\u0442\u0443\u0441 \u0421\u0435\u0440\u0432\u0435\u0440\u0430**\n");
//        sb.append(":green_circle: `\u041e\u043d\u043b\u0430\u0439\u043d       :  %02d \u0447\u0435\u043b\u043e\u0432\u0435\u043a`\n");
//        sb.append(":calendar: `\u041e\u0442 \u0440\u0435\u0441\u0442\u0430\u0440\u0442\u0430  :   %02d\u0447 %02d\u043c\u0438\u043d`\n");
//        if (gms.size() > 0) {
//            sb.append(":crossed_swords: `\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u0413\u041c\u044b :            `\n");
//            StringBuilder sb1 = new StringBuilder();
//            for (String gm: gms) {
//                String app = (sb1.length() > 0 ? ", " : "") + trimNick(gm);
//                if (sb1.length() == 0 || sb1.length() + app.length() <= 26)
//                    sb1.append(app);
//                else {
//                    sb.append(String.format(":zap: `%26s`\n", sb1.toString()));
//                    sb1 = new StringBuilder();
//                }
//            }
//            if (sb1.length() > 0)
//                sb.append(String.format(":zap: `%26s`\n", sb1.toString()));
//        } else sb.append(":crossed_swords: `\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u0413\u041c\u044b : \u041e\u0422\u0421\u0423\u0422\u0421\u0422\u0412\u0423\u042e\u0422`\n");
//        return String.format( sb.toString(), online,
//                restart / 60 / 60,
//                restart / 60 % 60 );
    }

    private static String noGmString(String gm) {
        int h = LocalTime.now().getHour();
        if (h >= 15 && h <= 22) {
            return ":anger: \u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442, \u043a\u0442\u043e-\u0442\u043e \u0438\u0437 " + gm + " \u043f\u043e\u043b\u0443\u0447\u0438\u0442 \u043f\u0438\u0437\u0434\u043e\u0432...";
        } else {
            return ":zzz: \u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442.";
        }
    }

    private static MessageEmbed serverInactive() {
        return new EmbedBuilder()
                .setTitle("**\u0421\u0442\u0430\u0442\u0443\u0441 \u0421\u0435\u0440\u0432\u0435\u0440\u0430**")
                .setDescription(":tools: :clock1: :skull_crossbones: :electric_plug: :grey_question:\n\u0421\u0435\u0440\u0432\u0435\u0440 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D, \u043F\u0435\u0440\u0435\u0437\u0430\u043F\u0443\u0441\u043A\u0430\u0435\u0442\u0441\u044F, \u043B\u0438\u0431\u043E \u043F\u0443\u0441\u0442\n*\u0410 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C, \u044F \u043F\u0440\u043E\u0441\u0442\u043E \u0435\u0433\u043E \u043D\u0435 \u0432\u0438\u0436\u0443, \u043D\u0435 \u0443\u043D\u044B\u0432\u0430\u0439\u0442\u0435!*")
                .build();
    }

    private MessageEmbed serverStatisticBasic() {
        return new EmbedBuilder()
                .setTitle("\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043A\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430")
                .setDescription("\u041F\u044B\u0442\u0430\u044E\u0441\u044C \u043F\u043E\u043B\u0443\u0447\u0438\u0442\u044C \u0438\u043D\u0444\u0443 \u0438\u0437 \u0411\u0414...\n*\u041F\u043E\u0445\u043E\u0436\u0435, \u0447\u0442\u043E \u043D\u0435 \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u043E\u0441\u044C..*")
                .build();
    }

    private MessageEmbed serverStatistic() {
        if (playersRecord <= 0) return serverStatisticBasic();
        if (playersTotal <= 0) return serverStatisticBasic();
        return new EmbedBuilder()
                .setColor(Color.lightGray)
                .setTitle("\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043A\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430")
                .addField(":100: \u0420\u0435\u043A\u043E\u0440\u0434\u043D\u044B\u0439 \u041E\u043D\u043B\u0430\u0439\u043D", countPlayers(playersRecord), false)
                .addField(":card_box: \u0418\u0433\u0440\u043E\u043A\u043E\u0432 \u0437\u0430 \u0432\u0441\u0435 \u0432\u0440\u0435\u043C\u044F", countPlayers(playersTotal), false)
                .build();
    }

    private static String countPlayers(int count) {
        int ccount =(count % 100);
        if ((ccount < 10 || ccount > 20) && ccount % 10 >= 2 && ccount % 10 <= 4)
            return String.format("%02d \u0447\u0435\u043B\u043E\u0432\u0435\u043A\u0430", count);
        else return String.format("%02d \u0447\u0435\u043B\u043E\u0432\u0435\u043A", count);
    }
}
