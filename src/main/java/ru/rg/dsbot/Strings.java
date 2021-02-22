package ru.rg.dsbot;

import ru.zont.dsbot2.UTF8Control;

import javax.annotation.PropertyKey;
import java.util.ResourceBundle;

public class Strings {
    public static final ResourceBundle STR_CORE = ResourceBundle.getBundle("strings", new UTF8Control());

    public static class STR {
        public static String getString(@PropertyKey String key) {
            if (STR_CORE.containsKey(key))
                return STR_CORE.getString(key);
            return key;
        }
    }
}
