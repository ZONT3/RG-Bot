package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.SubprocessListener;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Messages;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Exec extends CommandAdapter {
    private SubprocessListener sl = null;
    private MessageChannel workingChannel = null;

    private String name;
    private ArrayList<Message> stdoutMessages = new ArrayList<>();
    private ArrayList<Message> stderrMessages = new ArrayList<>();

    public Exec() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        workingChannel = event.getChannel();
        String input = Commands.parseInputRaw(this, event);
        Pattern pattern = Pattern.compile("[^\\w]*```(java|python)\\n((.|\\n)+)```[^\\w]*");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String lang = matcher.group(1);
            String code = matcher.group(2).replaceAll("\\\\`", "`");
            throw new NotImplementedException();
        } else if (input.startsWith("--cmd ")) {
            input = input.replaceFirst("--cmd ", "");
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidArgumentException("Corrupted input, may be empty", false);
            connectSL(new SubprocessListener(args[0], "cmd /c " + input));
        }
    }

    private void connectSL(SubprocessListener sl) {
        sl.setOnStdout(this::appendStdout);
        sl.setOnStderr(this::appendStderr);
        sl.setOnError(this::onError);
        sl.setOnFinish(this::onFinish);
        this.sl = sl;
    }

    private Void appendStdout(String lines) {
        appendOut(lines, stdoutMessages, "stdout");
        return null;
    }

    private Void appendStderr(String lines) {
        appendOut(lines, stderrMessages, "stderr");
        return null;
    }

    private void appendOut(String lines, List<Message> outList, String type) {
        if (!checkWChPrint()) return;
        MessageEmbed newState;
        try {
            Message last = getLastOutputMsg(outList);
            newState = appendContent(getContent(last), lines);
            last.editMessage(newState).complete();
        } catch (IllegalArgumentException e) {
            newOutputs(workingChannel, lines, outList, type);
        }
    }

    private MessageEmbed getContent(Message lastOutputMsg) {
        List<MessageEmbed> embeds = lastOutputMsg.getEmbeds();
        if (embeds.size() > 0) return embeds.get(0);
        else throw new NullPointerException("No embeds");
    }

    private void newOutputs(MessageChannel workingChannel, String lines, List<Message> outList, String type) {
        // TODO
    }

    private EmbedBuilder basicOutputMsg(List<Message> outList, int offset, String type) {
        return new EmbedBuilder().setTitle(nextTitle(outList, offset, type))
                .setColor(type.equalsIgnoreCase("stdout") ? 0x311b92 : 0xBB0000);
    }

    private String nextTitle(List<Message> outList, int offset, String type) {
        return name + " " + type + " #" + (outList.size() + offset);
    }

    private MessageEmbed appendContent(MessageEmbed lastOutputMsg, String lines) throws IllegalArgumentException {
        // TODO
    }

    private Message getLastOutputMsg(List<Message> outList) {
        // TODO
        return null;
    }

    private Void onError(Exception e) {
        if (!checkWChPrint()) return null;
        Messages.printError(workingChannel, "Error in SubprocessListener", Messages.describeException(e));
        return null;
    }

    private boolean checkWChPrint() {
        if (workingChannel == null) {
            new NullPointerException("Working channel").printStackTrace();
            return false;
        }
        return true;
    }

    private Void onFinish(int code) {
        if (!checkWChPrint()) return null;
        // TODO
        return null;
    }

    @Override
    public String getCommandName() {
        return "exec";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getSynopsis() {
        return "exec <input>"; // TODO
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.cmd.desc");
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return false;
    }
}
