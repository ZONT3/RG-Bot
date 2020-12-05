package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.NotImplementedException;
import ru.zont.rgdsb.SubprocessListener;
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.command.ExternalCallable;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Exec extends CommandAdapter implements ExternalCallable {
    private static long nextPid = 1;
    private static final Map<Long, ExecHandler> processes = Collections.synchronizedMap(new HashMap<>());

    public Exec() throws RegisterException {
        super();
    }

    @Override
    public void call(Commands.Input inputObj) {
        MessageReceivedEvent event = inputObj.getEvent();
        if (event == null) throw new IllegalStateException("Provided input must contain event");
        MessageChannel channel = event.getChannel();
        String input = inputObj.getRaw();

        Pattern pattern = Pattern.compile("[^\\w]*(--?\\w+ )*[^\\w]*```(\\w+)\\n((.|\\n)+)```[^\\w]*");
        Matcher matcher = pattern.matcher(input);

        String lineToExec;
        String name;
        ExecHandler.Parameters params = new ExecHandler.Parameters();
        SubprocessListener.Builder builder = new SubprocessListener.Builder();
        if (matcher.find()) {
            String lang = matcher.group(2);
            String code = matcher.group(3).replaceAll("\\\\`", "`");

            boolean buff = inputObj.hasOpt("b", true) || inputObj.hasOpt("buffer", true);
            boolean silent = inputObj.hasOpt("s", true) || inputObj.hasOpt("silent", true);
            boolean single = inputObj.hasOpt("S", true) || inputObj.hasOpt("single", true);
            if (silent) {
                params.verbose = false;
                event.getMessage().delete().queue();
            }
            if (single) params.single_window = true;

            File tempFile;
            switch (lang) {
                case "py":
                case "python":
                    name = "Python code";
                    tempFile = toTemp(code);
                    lineToExec = String.format("python -X utf8 %s\"%s\"",
                            buff ? "" : "-u ",
                            tempFile.getAbsolutePath());
                    break;
                case "java":
                    name = "Java code";
                    throw new NotImplementedException();
                default: throw new RuntimeException("Unknown programming language");
            }
            params.onVeryFinish = param -> {
                if (!tempFile.delete())
                    System.err.println("Cannot delete temp file!");
            };
        } else if (inputObj.hasOpt("c", true) || inputObj.hasOpt("cmd", true)) {
            input = inputObj.stripPrefixOpts();
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidArgumentException("Corrupted input, may be empty", false);
            name = args[0];
            lineToExec = "cmd /c " + input;
            params.verbose = false;
            builder.setCharset(Charset.forName("866"));
        } else {
            input = inputObj.stripPrefixOpts();
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidArgumentException("Corrupted input, may be empty", false);
            name = args[0];
            lineToExec = input;
        }

        if (name != null)
            newHandler(builder.build(name, lineToExec), params, channel);
        else Messages.printError(channel, "Error", "Cannot handle input");
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        call(Commands.parseInput(this, event));
    }

    public synchronized void newHandler(SubprocessListener sl, ExecHandler.Parameters params, MessageChannel channel) {
        processes.put(nextPid, new ExecHandler(sl, nextPid, channel, params));
        nextPid++;
    }

    private File toTemp(String code) {
        try {
            File f = File.createTempFile("tempCode", ".py");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(code.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ExecHandler findProcess(long id) {
        return processes.getOrDefault(id, null);
    }

    public static void removeProcess(long pid) {
        processes.remove(pid);
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
        return STR.getString("comm.exec.desc");
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return false;
    }
}
