package ru.zont.rgdsb.command.exec;

import javafx.util.Callback;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.SubprocessListener;
import ru.zont.rgdsb.tools.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExecHandler {
    public static final int OUT_MAX_LEN = 2000;

    private final MessageChannel workingChannel;
    private final long started = System.currentTimeMillis();

    private final String name;
    private final OutList stdoutMessages = new OutList();
    private final OutList stderrMessages = new OutList();

    private final Parameters params;
    static class Parameters {
        boolean alwaysPrintExit = true;
        Callback<Void, Void> onVeryFinish = null;
    }

    public ExecHandler(@NotNull SubprocessListener sl, @NotNull MessageChannel workingChannel, Parameters params) {
        this.workingChannel = workingChannel;
        this.params = params;
        name = sl.getProcName();
        connectSL(sl);
        sl.start();
    }

    private void connectSL(SubprocessListener sl) {
        sl.setOnStdout(this::appendStdout);
        sl.setOnStderr(this::appendStderr);
        sl.setOnError(this::onError);
        sl.setOnFinish(this::onFinish);
    }

    private Void onError(Exception e) {
        if (checkWChPrint()) return null;
        Messages.printError(workingChannel, "Error in SubprocessListener", Messages.describeException(e));
        return null;
    }

    private Void onFinish(int code) {
        if (checkWChPrint()) return null;

        if (code != 0 || params.alwaysPrintExit) {
            long millis = System.currentTimeMillis() - started;
            long sec = millis / 1000;
            workingChannel.sendMessage(new EmbedBuilder()
                    .setColor(code == 0 ? 0x00DA00 : 0x8F0000)
                    .setTitle("Process finished")
                    .setDescription(String.format(
                            "Duration: %d.%ds\n" +
                                    "Exit code: `%d`",
                            sec, millis % 1000, code
                    )).build()).queue();
        }

        if (params.onVeryFinish != null)
            params.onVeryFinish.call(null);

        return null;
    }

    private Void appendStdout(String lines) {
        appendOut(lines, stdoutMessages, "stdout");
        return null;
    }

    private Void appendStderr(String lines) {
        appendOut(lines, stderrMessages, "stderr");
        return null;
    }

    private synchronized void appendOut(String lines, OutList outList, String type) {
        if (checkWChPrint()) return;

        try {
            OutListEntry lastOutputMsg = getLastOutputMsg(outList);
            MessageEmbed embed = lastOutputMsg != null ? getEmbed(lastOutputMsg.getMsg()) : null;
            String content = lastOutputMsg != null ? lastOutputMsg.getVal() : "";
            boolean first = true;
            for (String l: safeAddLines(content + lines)) { // TODO очень странное поведение при переполнении ФИКС REQUIRED!!!
                if (first && lastOutputMsg != null) {
                    lastOutputMsg.edit(l, embed);
                    first = false;
                } else {
                    sendOutput(outList, type, l);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Messages.printError(workingChannel, "Error in ExecHandler", Messages.describeException(e));
        }
    }

    private MessageEmbed getEmbed(Message msg) {
        List<MessageEmbed> embeds = msg.getEmbeds();
        if (embeds.size() > 0) return embeds.get(0);
        else throw new NullPointerException("No embeds");
    }

    private List<String> safeAddLines(String lines) {
        ArrayList<String> res = new ArrayList<>();
        StringBuilder nextLines = new StringBuilder();
        for (String line: lines.split("\n")) {
            if (nextLines.length() + line.length() <= OUT_MAX_LEN) {
                nextLines.append(line).append("\n");
            } else {
                List<String> list = splitString(nextLines);
                if (list.size() == 1)
                    res.add(nextLines.toString());
                else {
                    res.addAll(list.subList(0, list.size() - 1));
                    nextLines = new StringBuilder(list.get(list.size() - 1))
                            .append("\n").append(line).append("\n");
                }
            }
        }
        if (!nextLines.toString().isEmpty())
            res.addAll(splitString(nextLines));
        return res;
    }

    @NotNull
    private List<String> splitString(CharSequence nextLines) {
        return Arrays.asList(nextLines.toString().split("(?<=\\G.{" + OUT_MAX_LEN + "})"));
    }

    private void sendOutput(OutList outList, String type, String lines) {
        outList.add(
                workingChannel.sendMessage(basicOutputMsg(outList, type)
                        .setDescription("```" + lines + "```")
                        .build()
                ).complete(), lines);
    }

    private EmbedBuilder basicOutputMsg(OutList outList, String type) {
        return new EmbedBuilder().setTitle(nextTitle(outList, type))
                .setColor(type.equalsIgnoreCase("stdout") ? 0x311b92 : 0xBB0000);
    }

    private String nextTitle(OutList outList, String type) {
        return name + " " + type + " #" + outList.size();
    }

    private OutListEntry getLastOutputMsg(OutList outList) {
        if (outList.isEmpty()) return null;
        return outList.get(outList.size() - 1);
    }

    private boolean checkWChPrint() {
        if (workingChannel == null) {
            new NullPointerException("Working channel").printStackTrace();
            return true;
        }
        return false;
    }

    private static class OutList extends ArrayList<OutListEntry> {
        public void add(Message m, String s) {
            add(new OutListEntry(m, s));
        }
    }

    private static class OutListEntry {
        private final Message m;
        private String s;

        public OutListEntry(Message m, String s) {
            this.m = m;
            this.s = s;
        }

        public void edit(String ns, MessageEmbed embed) {
            m.editMessage(
                    new EmbedBuilder(embed)
                            .setDescription("```" + ns + "```")
                            .build()
            ).complete();
            s = ns;
        }

        public Message getMsg() {
            return m;
        }

        public String getVal() {
            return s;
        }
    }
}
