package ru.zont.rgdsb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class SubprocessListener extends Thread {

    private String name;

    public SubprocessListener(CharSequence name, CharSequence execLine) {
        this.name = name.toString();
    }

    @Override
    public void run() {

    }

    public static void main(String[] args) throws IOException {
        testProcess("java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -cp \"D:\\Users\\ZONT_\\Documents\" Test");
        System.out.println("");
        testProcess("python -u \"D:\\Users\\ZONT_\\Documents\\Test.py\"");
    }

    private static void testProcess(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        StreamListener.create(process, process.getInputStream(),
                line -> System.out.printf("A:%s RL: %s\n", process.isAlive() ? "T" : "F", line));
        StreamListener.create(process, process.getErrorStream(),
                line -> System.err.printf("A:%s RL: %s\n", process.isAlive() ? "T" : "F", line));
        while (process.isAlive()) {
            try { Thread.sleep(10); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        System.out.println("Process dead.");
    }

    private static class StreamListener extends Thread {
        private final Process process;
        private final InputStream stream;
        private final OnLineReceived listener;

        private static StreamListener create(Process process, InputStream stream, OnLineReceived listener) {
            return new StreamListener(process, stream, listener);
        }

        private StreamListener(Process process, InputStream stream, OnLineReceived listener) {
            this.process = process;
            this.stream = stream;
            this.listener = listener;
            start();
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(stream);
            while (process.isAlive() || scanner.hasNext()) {
                if (scanner.hasNext())
                    listener.onLineReceived(scanner.nextLine());
            }
        }
    }

    private interface OnLineReceived {
        void onLineReceived(String line);
    }
}
