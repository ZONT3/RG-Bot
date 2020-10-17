package ru.zont.rgdsb;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;

public class Commands {
    public static String[] parseArgs(InteractAdapter adapter, MessageReceivedEvent event) {
        String msg = parseInput(adapter, event).trim();
        if (msg.isEmpty()) return new String[0];
        return ArgumentTokenizer.tokenize(msg).toArray(new String[0]);
    }

    public static String parseInput(InteractAdapter adapter, MessageReceivedEvent event) {
        String msg = event.getMessage().getContentStripped();
        if (!msg.startsWith(InteractAdapter.getPrefix() + adapter.getCommandName()) && !msg.startsWith(adapter.getCommandName()))
            throw new IllegalStateException("Provided event does not contain a command request");
        if (msg.startsWith(InteractAdapter.getPrefix()))
            msg = msg.replaceFirst(InteractAdapter.getPrefix() + adapter.getCommandName(), "");
        else msg = msg.replaceFirst(adapter.getCommandName(), "");
        return msg;
    }

    public static HashMap<String, InteractAdapter> getAllCommands() {
        HashMap<String, InteractAdapter> res = new HashMap<>();
        for (InteractAdapter a: Main.commandAdapters)
            if (!a.getCommandName().isEmpty())
                res.put(a.getCommandName(), a);
        return res;
    }

    public static InteractAdapter getCommandByName(String name) {
        for (InteractAdapter a: Main.commandAdapters)
            if (a.getCommandName().equals(name))
                return a;
        return null;
    }
}
