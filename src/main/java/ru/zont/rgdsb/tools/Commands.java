package ru.zont.rgdsb.tools;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.command.ExternalCallable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Commands {

    public static String[] parseArgs(CommandAdapter adapter, MessageReceivedEvent event) {
        String msg = parseInputRaw(adapter, event).trim();
        if (msg.isEmpty()) return new String[0];
        return ArgumentTokenizer.tokenize(msg).toArray(new String[0]);
    }

    public static String[] parseArgs(CharSequence raw) {
        return ArgumentTokenizer.tokenize(raw.toString()).toArray(new String[0]);
    }

    public static String parseInputRaw(CommandAdapter adapter, MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        if (!msg.startsWith(Configs.getPrefix() + adapter.getCommandName()) && !msg.startsWith(adapter.getCommandName()))
            throw new IllegalStateException("Provided event does not contain a command request");
        if (msg.startsWith(Configs.getPrefix()))
            msg = msg.replaceFirst(Configs.getPrefix() + adapter.getCommandName(), "");
        else msg = msg.replaceFirst(adapter.getCommandName(), "");
        return msg;
    }

    public static Input parseInput(CommandAdapter adapter, MessageReceivedEvent event) {
        return new Input(parseInputRaw(adapter, event), event);
    }

    public static HashMap<String, CommandAdapter> getAllCommands() {
        HashMap<String, CommandAdapter> res = new HashMap<>();
        for (CommandAdapter a: Globals.commandAdapters)
            if (!a.getCommandName().isEmpty())
                res.put(a.getCommandName(), a);
        return res;
    }

    public static CommandAdapter forName(String command) {
        HashMap<String, CommandAdapter> comms = Commands.getAllCommands();
        for (Map.Entry<String, CommandAdapter> entry: comms.entrySet())
            if (command.toLowerCase().equals(entry.getKey().toLowerCase()))
                return entry.getValue();
        return null;
    }

    public static CommandAdapter forClass(Class<? extends CommandAdapter> klass) {
        HashMap<String, CommandAdapter> comms = Commands.getAllCommands();
        for (Map.Entry<String, CommandAdapter> entry: comms.entrySet())
            if (entry.getValue().getClass().equals(klass))
                return entry.getValue();
        return null;
    }

    public static void call(Class<? extends CommandAdapter> klass, String inputRaw, MessageReceivedEvent event) {
        CommandAdapter adapter = forClass(klass);
        if (adapter != null)
            if (adapter instanceof ExternalCallable)
                ((ExternalCallable) adapter).call(new Input(inputRaw, event));
    }

    public static class Input {
        private final String raw;
        private ArrayList<String> args;
        private ArrayList<String> options;

        private final MessageReceivedEvent event;

        private Input(String raw, MessageReceivedEvent event) {
            this.event = event;
            this.raw = raw;
        }

        public Input(String raw) {
            this.raw = raw;
            event = null;
        }

        private void checkBuild() {
            if (args != null && options != null) return;
            args = new ArrayList<>();
            options = new ArrayList<>();
            for (String s: parseArgs(raw)) {
                if (s.startsWith("--"))
                    options.add(s.toLowerCase());
                else if (s.startsWith("-"))
                    parseOpts(s);
                else args.add(s);
            }
        }

        private void parseOpts(String s) {
            for (char c: s.substring(1).toCharArray()) options.add(c + "");
        }

        public boolean hasOpt(String o) {
            checkBuild();
            return options.contains(o);
        }

        public ArrayList<String> getArgs() {
            checkBuild();
            return args;
        }

        public ArrayList<String> getOptions() {
            checkBuild();
            return options;
        }

        public String getRaw() {
            return raw;
        }

        public MessageReceivedEvent getEvent() {
            return event;
        }
    }
}
