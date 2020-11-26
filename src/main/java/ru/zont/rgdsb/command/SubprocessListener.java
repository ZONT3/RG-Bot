package ru.zont.rgdsb.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.tools.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class SubprocessListener extends Thread {
    public static final int PRINT_INTERVAL = 500;

    private final CharSequence execLine;
    private final MessageChannel channel;

    private final String name;
    private Process proc = null;
    private ArrayList<Message> outputMessages;

    private static String buildName(CharSequence execLine) {
        return execLine.toString().split(" ")[0];
    }

    public SubprocessListener(CharSequence execLine, MessageChannel channel) {
        super("SubprocessListener - " + buildName(execLine));
        name = "SubprocessListener - " + buildName(execLine);

        this.execLine = execLine;
        this.channel = channel;
        outputMessages = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            proc = Runtime.getRuntime().exec(execLine.toString());

            try (Scanner in = new Scanner(proc.getInputStream(), "windows-1251")) {
                StringBuilder buffer = new StringBuilder();
                while (in.hasNext() || isProcAlive() || !buffer.toString().isEmpty()) {
                    if (in.hasNext())
                        buffer.append(in.nextLine()).append("\n");
                    else if (!buffer.toString().isEmpty()) {
                        addOut(buffer);
                        buffer = new StringBuilder();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Messages.printError(channel, "Error in SubprocessListener", e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
            }

        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e.getLocalizedMessage(), e);
        }
    }

    private boolean isProcAlive() {
        if (proc == null) return false;
        return proc.isAlive();
    }

    private String newMessageTitle() {
        return buildName(execLine) + " command output [" + outputMessages.size() + "]";
    }

    private void addOut(CharSequence nextLine) {
        Message lastMsg = outputMessages.size() > 0 ? outputMessages.get(outputMessages.size() - 1) : null;
        MessageEmbed embed = lastMsg != null
                ? lastMsg.getEmbeds().get(0)
                : newMessage();
        try {
            MessageEmbed res = new EmbedBuilder(embed).appendDescription(nextLine).appendDescription("\n").build();
            if (lastMsg != null) lastMsg.editMessage(res).complete();
            else outputMessages.add(channel.sendMessage(res).complete());
        } catch (IllegalArgumentException e) {
            MessageEmbed res = new EmbedBuilder(newMessage()).appendDescription(nextLine).appendDescription("\n").build();
            outputMessages.add(channel.sendMessage(res).complete());
        }
    }

    @NotNull
    private MessageEmbed newMessage() {
        return new EmbedBuilder().setTitle(newMessageTitle()).setColor(0x111111).build();
    }
}
