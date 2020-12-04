package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.SubprocessListener;
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.tools.Commands;
import ru.zont.rgdsb.tools.Messages;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.rgdsb.tools.Strings.STR;

public class Exec extends CommandAdapter {

    public Exec() throws RegisterException {
        super();
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String input = Commands.parseInputRaw(this, event);
        Pattern pattern = Pattern.compile("[^\\w]*```(java|python)\\n((.|\\n)+)```[^\\w]*");
        Matcher matcher = pattern.matcher(input);

        String lineToExec;
        String name;
        ExecHandler.Parameters params = new ExecHandler.Parameters();
        SubprocessListener.Builder builder = new SubprocessListener.Builder();
        if (matcher.find()) {
            String lang = matcher.group(1);
            String code = matcher.group(2).replaceAll("\\\\`", "`");
            File tempFile;
            switch (lang) {
                case "python":
                    name = "Py code";
                    tempFile = toTemp(code);
                    lineToExec = "python -X utf8 -u \"" + tempFile.getAbsolutePath() + "\"";
                    break;
                case "java":
                    name = "Java code";
                    throw new NotImplementedException();

                default: throw new RuntimeException("Unknown programming language");
            }
            params.onVeryFinish = param -> {
                if (!tempFile.delete())
                    System.err.println("Cannot delete temp file!");
                return null;
            };
        } else if (input.startsWith("--cmd ")) {
            input = input.replaceFirst("--cmd ", "");
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidArgumentException("Corrupted input, may be empty", false);
            name = args[0];
            lineToExec = "cmd /c " + input;
            params.alwaysPrintExit = false;
            builder.setCharset(Charset.forName("866"));
        } else {
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidArgumentException("Corrupted input, may be empty", false);
            name = args[0];
            lineToExec = input;
        }

        if (name != null)
            new ExecHandler(builder.build(name, lineToExec), event.getChannel(), params);
        else Messages.printError(event.getChannel(), "Error", "Cannot handle input");
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
