package ru.zont.rgdsb;

import javafx.util.Callback;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SubprocessListener extends Thread {

    private final String name;
    private final String execLine;

    private int exitStatus = -1;

    private Callback<String, Void> onStdout = null;
    private Callback<String, Void> onStderr = null;
    private Callback<Integer, Void> onFinish = null;
    private Callback<Exception, Void> onError = null;

    public SubprocessListener(CharSequence name, CharSequence execLine) {
        super("SpL: " + name);
        this.name = name.toString();
        this.execLine = execLine.toString();
    }

    @Override
    public void run() {
        try {
            Process process = Runtime.getRuntime().exec(execLine);
            new StreamListener(process, process.getInputStream(), line -> {
                if (onStdout != null)
                    onStdout.call(line);
                return null;
            });
            new StreamListener(process, process.getErrorStream(), line -> {
                if (onStderr != null)
                    onStderr.call(line);
                return null;
            });
            process.waitFor();
            exitStatus = process.exitValue();
            if (onFinish != null)
                onFinish.call(exitStatus);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            if (onError != null)
                onError.call(e);
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

        private StreamListener(Process process, InputStream stream, Callback<String, Void> listener) {
            super("SpL.StL: " + name);
            this.process = process;
            this.stream = stream;
            this.callback = listener;
            start();
        }

        @Override
        public void run() {
            BufferedReader scanner = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder buffer = new StringBuilder();
            try {
                while (process.isAlive() || scanner.ready() || !buffer.toString().isEmpty()) {
                    if (scanner.ready()) {
                        // Если уже есть какой-то ввод, сначала переносим строку
                        if (!buffer.toString().isEmpty())
                            buffer.append('\n');
                        // Добавляем в буффер ввод
                        buffer.append(scanner.readLine());
                    } else if (!buffer.toString().isEmpty()) {
                        // Если в этот раз нету данных, но в прошлый раз - был,
                        // значит надо отправить содержимое буффера
                        callback.call(buffer.toString());
                        // и очистить буффер
                        buffer = new StringBuilder();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
