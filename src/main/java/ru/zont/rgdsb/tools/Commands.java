package ru.zont.rgdsb.tools;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.command.ExternalCallable;

import java.util.*;

public class Commands {

    public static String[] parseArgs(CommandAdapter adapter, MessageReceivedEvent event) {
        String msg = parseRaw(adapter, event).trim();
        if (msg.isEmpty()) return new String[0];
        return ArgumentTokenizer.tokenize(msg).toArray(new String[0]);
    }

    public static List<String> parseArgs(CharSequence raw) {
        return ArgumentTokenizer.tokenize(raw.toString().trim());
    }

    public static String parseRaw(CommandAdapter adapter, MessageReceivedEvent event) {
        return parseRaw(adapter, event.getMessage().getContentRaw());
    }

    public static String parseRaw(CommandAdapter adapter, CharSequence source) {
        String msg = source.toString();

        if (msg.startsWith(Configs.getPrefix() + adapter.getCommandName()))
            msg = msg.replaceFirst(Configs.getPrefix() + adapter.getCommandName(), "");
        else if (msg.startsWith(adapter.getCommandName()))
            msg = msg.replaceFirst(adapter.getCommandName(), "");
        else throw new IllegalStateException("Provided event does not contain a command request");

        if (msg.startsWith(" "))
            msg = msg.replaceFirst(" +", "");
        return msg;
    }

    public static Input parseInput(CommandAdapter adapter, MessageReceivedEvent event) {
        return new Input(parseRaw(adapter, event), event);
    }

    public static Input makeInput(CommandAdapter adapter, CharSequence source) {
        return new Input(parseRaw(adapter, source));
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
        if (adapter != null) {
            if (adapter instanceof ExternalCallable) {
                ((ExternalCallable) adapter).call(new Input(inputRaw, event));
                return;
            }
        }
        throw new NoSuchElementException("Cannot find command for class " + klass.getSimpleName());
    }

    public static class Input implements Iterable<String> {
        private final String raw;
        private List<String> allArgs;
        private ArrayList<String> args;
        private ArrayList<String> opts;

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
            if (args != null && opts != null && allArgs != null) return;
            args = new ArrayList<>();
            opts = new ArrayList<>();
            allArgs = parseArgs(raw);
            for (String s: allArgs) {
                if (isOption(s))
                    parseOpts(s, opts);
                else args.add(s);
            }
        }

        private void parseOpts(String s, List<String> list) {
            if (s.startsWith("--"))
                list.add(s.substring(2));
            else for (char c: s.substring(1).toCharArray()) list.add(c + "");
        }

        public boolean hasOpt(String o) {
            return hasOpt(o, false);
        }

        public boolean hasOpt(String o, boolean prefix) {
            checkBuild();
            if (!prefix) return opts.contains(o);
            else         return getPrefixOpts().contains(o);
        }

        public ArrayList<String> getArgs() {
            checkBuild();
            return args;
        }

        public List<String> getAllArgs() {
            checkBuild();
            return allArgs;
        }

        public ArrayList<String> getOptions() {
            checkBuild();
            return opts;
        }

        public ArrayList<String> getPrefixOpts() {
            ArrayList<String> res = new ArrayList<>();
            for (String s: this) {
                if (isOption(s)) parseOpts(s, res);
                else return res;
            }
            return res;
        }

        public static boolean isOption(CharSequence sequence) {
            String s = sequence.toString();
            if (s.startsWith(" "))
                s = s.replaceFirst(" +", "");
            return s.startsWith("-") && !s.startsWith("---");
        }

        public String getRaw() {
            return raw;
        }

        public MessageReceivedEvent getEvent() {
            return event;
        }

        public String stripPrefixOpts() {
            if (!raw.startsWith("-") || raw.startsWith("---")) return raw;
            return raw.replaceFirst("(--?[^ ]+ +)+", "");
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return new Iter();
        }

        private class Iter implements Iterator<String> {
            private int pointer = 0;

            @Override
            public boolean hasNext() {
                return pointer < args.size();
            }

            @Override
            public String next() {
                return allArgs.get(pointer++);
            }
        }
    }
}
