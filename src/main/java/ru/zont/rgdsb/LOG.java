package ru.zont.rgdsb;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LOG {
    public static void d(String s) {
        System.out.println(timestamp() + s);
    }

    public static void d(String s, Object... args) {
        System.out.printf(timestamp() + s + "\n", args);
    }

    private static String timestamp() {
        return new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss] ").format(new Timestamp(System.currentTimeMillis()));
    }
}
