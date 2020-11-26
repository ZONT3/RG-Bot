package ru.zont.rgdsb.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class LOG {
    private static final File logfile;
    private static final File logpath = new File("log");

    static {
        logfile = new File(logpath, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(Date.from(Instant.now())) + ".log");
        if (logfile.exists() && !logfile.delete())
            throw new RuntimeException("Cannot delete corresponding log file");

        try {
            if ((!logpath.exists() && !logpath.mkdir()) || !logfile.createNewFile())
                throw new RuntimeException("Cannot create logfile");
        } catch (IOException e) {
            throw new RuntimeException("Cannot create logfile", e);
        }
    }

    public static void d(String s) {
        String str = timestamp() + s;
        System.out.println(str);
        appendFile(str + "\n");
    }

    public static void d(String s, Object... args) {
        String str = String.format(timestamp() + s, args);
        System.out.println(str);
        appendFile(str + "\n");
    }

    private static String timestamp() {
        return new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ").format(new Timestamp(System.currentTimeMillis()));
    }

    private static void appendFile(String str) {
        if (logfile == null) return;
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(logfile, true));
            output.append(str);
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
