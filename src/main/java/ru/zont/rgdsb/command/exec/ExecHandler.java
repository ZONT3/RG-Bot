package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.SubprocessListener;
import ru.zont.rgdsb.tools.Messages;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class ExecHandler {
    public static final int OUT_MAX_LEN = 2000;

    private final MessageChannel workingChannel;
    private final long started = System.currentTimeMillis();

    private final long pid;
    private final String name;
    private final OutList stdoutMessages = new OutList();
    private final OutList stderrMessages = new OutList();

    private final Parameters params;
    private final SubprocessListener sl;
    private Message statusMessage;

    static class Parameters {
        boolean verbose = true;
        Consumer<Void> onVeryFinish = null;
    }

    public ExecHandler(@NotNull SubprocessListener sl, long pid, @NotNull MessageChannel workingChannel, Parameters params) {
        this.workingChannel = workingChannel;
        this.params = params;
        this.sl = sl;
        this.pid = pid;

        name = this.sl.getProcName();
        connectSL(this.sl);
        sl.start();
        if (params.verbose)
            printStart();
    }

    private void printStart() {
        statusMessage = workingChannel.sendMessage(new EmbedBuilder()
                .setTitle(String.format("Process [%d] %s started", pid, name))
                .setColor(0xE0E0E0)
                .setTimestamp(Instant.now())
                .build()).complete();
        statusMessage.addReaction("\u23F3").queue();
    }

    private void printEnd(int code) {
        long millis = System.currentTimeMillis() - started;
        long sec = millis / 1000;
        workingChannel.sendMessage(new EmbedBuilder()
                .setColor(code == 0 ? 0x00DA00 : 0x8F0000)
                .setTitle(String.format("Process [%d] finished", pid))
                .setDescription(String.format(
                        "Name: %s\n" +
                                "Duration: %d.%03ds\n" +
                                "Exit code: `%d`",
                        name, sec, millis % 1000, code
                )).build()).queue();
    }

    private void connectSL(SubprocessListener sl) {
        sl.setOnStdout(this::appendStdout);
        sl.setOnStderr(this::appendStderr);
        sl.setOnError(this::onError);
        sl.setOnFinish(this::onFinish);
    }

    private void onError(Exception e) {
        if (checkWChPrint()) return;
        Messages.printError(workingChannel, "Error in SubprocessListener", Messages.describeException(e));
    }

    private void onFinish(int code) {
        Exec.removeProcess(pid);
        if (params.onVeryFinish != null)
            params.onVeryFinish.accept(null);

        if (statusMessage != null) {
            statusMessage.removeReaction("\u23F3").queue();
            Messages.addOK(statusMessage);
        }

        if (checkWChPrint()) return;
        if (code != 0 || params.verbose) {
            printEnd(code);
        }

    }

    private void appendStdout(String lines) {
        appendOut(lines, stdoutMessages, "stdout");
    }

    private void appendStderr(String lines) {
        appendOut(lines, stderrMessages, "stderr");
    }

    private synchronized void appendOut(String lines, OutList outList, String type) {
        if (checkWChPrint()) return;

        try {
            OutListEntry lastOutputMsg = getLastOutputMsg(outList);
            MessageEmbed embed = lastOutputMsg != null ? getEmbed(lastOutputMsg.getMsg()) : null;
            String content = lastOutputMsg != null ? lastOutputMsg.getVal() : "";
            boolean first = true;
            for (String l: safeAddLines(content + lines)) {
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
        for (String line: lines.split("(?<=\\n)")) {
            if (nextLines.length() + line.length() <= OUT_MAX_LEN) {
                nextLines.append(line);
            } else {
                List<String> list = splitString(nextLines);
                if (list.size() == 1) {
                    res.add(nextLines.toString());
                    nextLines = new StringBuilder(line);
                } else {
                    res.addAll(list.subList(0, list.size() - 1));
                    nextLines = new StringBuilder(list.get(list.size() - 1)).append(line);
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
                        .setDescription("```\n" + lines + "```")
                        .build()
                ).complete(), lines);
    }

    private EmbedBuilder basicOutputMsg(OutList outList, String type) {
        return new EmbedBuilder().setTitle(nextTitle(outList, type))
                .setColor(type.equalsIgnoreCase("stdout") ? 0x311b92 : 0xBB0000);
    }

    private String nextTitle(OutList outList, String type) {
        return String.format("[%d] %s %s #%d", pid, name, type, outList.size());
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

    public long getPid() {
        return pid;
    }

    public void terminate() {
        sl.terminate();
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
                            .setDescription("```\n" + ns + "```")
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
