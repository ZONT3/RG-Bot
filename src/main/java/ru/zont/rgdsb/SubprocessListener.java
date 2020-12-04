package ru.zont.rgdsb;

import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SubprocessListener extends Thread {

    public static final int LISTEN_DELAY = 200;
    private final String name;
    private final String execLine;

    private int exitStatus = -1337;

    private Callback<String, Void> onStdout = null;
    private Callback<String, Void> onStderr = null;
    private Callback<Integer, Void> onFinish = null;
    private Callback<Exception, Void> onError = null;

    private Charset charset = StandardCharsets.UTF_8;

    public static class Builder {
        private Charset charset;

        public Builder setCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public SubprocessListener build(@NotNull CharSequence name, @NotNull CharSequence execLine) {
            SubprocessListener l = new SubprocessListener(name, execLine);
            if (charset != null) l.charset = charset;
            return l;
        }
    }

    public SubprocessListener(@NotNull CharSequence name, @NotNull CharSequence execLine) {
        super("SpL: " + name);
        this.name = name.toString();
        this.execLine = execLine.toString();
    }

    @Override
    public void run() {
        Callback<Exception, Void> eCallback = e -> {
            e.printStackTrace();
            if (onError != null)
                onError.call(e);
            return null;
        };

        try {
            Process process = Runtime.getRuntime().exec(execLine);
            new StreamListener(process, process.getInputStream(), line -> {
                if (onStdout != null)
                    onStdout.call(line);
                return null;
            }, eCallback);
            new StreamListener(process, process.getErrorStream(), line -> {
                if (onStderr != null)
                    onStderr.call(line);
                return null;
            }, eCallback);
            process.waitFor();
            exitStatus = process.exitValue();
            if (onFinish != null)
                onFinish.call(exitStatus);
        } catch (Exception e) {
            eCallback.call(e);
        }
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getProcName() { return name; }

    public void setOnStdout(Callback<String, Void> onStdout) {
        this.onStdout = onStdout;
    }

    public void setOnStderr(Callback<String, Void> onStderr) {
        this.onStderr = onStderr;
    }

    public void setOnFinish(Callback<Integer, Void> onFinish) {
        this.onFinish = onFinish;
    }

    public void setOnError(Callback<Exception, Void> onError) {
        this.onError = onError;
    }

    private class StreamListener extends Thread {
        private final Process process;
        private final InputStream stream;
        private final Callback<String, Void> callback;
        private final Callback<Exception, Void> eCallback;

        private StreamListener(Process process, InputStream stream, Callback<String, Void> listener, Callback<Exception, Void> eCallback) {
            super("SpL.StL: " + name);
            this.process = process;
            this.stream = stream;
            this.callback = listener;
            this.eCallback = eCallback;
            start();
        }

        @Override
        public void run() {
            BufferedReader scanner = new BufferedReader(new InputStreamReader(stream, charset));
            StringBuilder buffer = new StringBuilder();
            try {
                long lstListen = 0;
                while (process.isAlive() || scanner.ready() || !buffer.toString().isEmpty()) {
                    if (scanner.ready()) {
                        buffer.append(scanner.readLine()).append("\n");
                    } else if (!buffer.toString().isEmpty()) {
                        long l = System.currentTimeMillis() - lstListen;
                        if (l < LISTEN_DELAY)
                            sleep(LISTEN_DELAY - l);
                        callback.call(buffer.toString());
                        lstListen = System.currentTimeMillis();
                        buffer = new StringBuilder();
                    }
                }
            } catch (Exception e) {
                eCallback.call(e);
            }
        }
    }


    public static void main(String[] args) throws IOException {
//            SubprocessListener test = new SubprocessListener("Test", "python -X utf8 -u \"D:\\Users\\ZONT_\\Documents\\Test.py\"");
        SubprocessListener test = new SubprocessListener("Test", "java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp \"D:\\Users\\ZONT_\\Documents\" Test2");
        PrintWriter writer = new PrintWriter(new FileOutputStream(new File("test.txt")), true);
        test.setOnStdout(param -> {
            System.out.println("STDOUT: " + param);
            writer.println(param);
            return null;
        });


        test.setOnStderr(param -> {
            System.err.println("STDERR: " + param);
            return null;
        });
        test.setOnFinish(param -> {
            System.out.println("EXIT CODE: " + param);
            return null;
        });
        test.setOnError(param -> {
            System.out.println("ERROR: " + param.getClass().getSimpleName() + ": " + param.getLocalizedMessage());
            return null;
        });
        test.start();
    }

}
