package ru.zont.rgdsb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesTools {
    public static final File DIR_PROPS = new File("properties");
    private static final String PROPS_COMMENT = "Properties of Right Games project's DS Bot Command";
    static final int CACHE_LIFETIME = 20000;
    private static Properties gPropertiesCache = null;
    private static long gPropertiesCacheTS = 0;

    public static String getRoleGmID() {
        String res = getGlobalProps().getProperty("ROLE_GM");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static String getChannelStatusID() {
        String res = getGlobalProps().getProperty("CHANNEL_STATUS");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static String getRoleGmmID() {
        String res = getGlobalProps().getProperty("ROLE_GMM");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static String getRolePlayerID() {
        String res = getGlobalProps().getProperty("ROLE_PLAYER");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static String getChannelPlayersID() {
        String res = getGlobalProps().getProperty("CHANNEL_PLAYERS");
        if (res.equals("0"))
            throw new IdNotProvidedException();
        return res;
    }

    public static void storeGlobalProps(Properties properties) {
        storeProps("global", properties);
        gPropertiesCache = properties;
        gPropertiesCacheTS = System.currentTimeMillis();
    }

    public static Properties getGlobalProps() {
        long current = System.currentTimeMillis();
        if (gPropertiesCache != null && current - gPropertiesCacheTS <= CACHE_LIFETIME)
            return gPropertiesCache;

        Properties def = getGlobalPropsDefaults();
        Properties res = getProps("global", def);
        gPropertiesCache = res;
        gPropertiesCacheTS = current;
        return res;
    }

    public static Properties getGlobalPropsDefaults() {
        return new Properties(){{
            setProperty("ROLE_PLAYER", "0");
            setProperty("ROLE_GM", "0");
            setProperty("ROLE_GGM", "0");
            setProperty("CHANNEL_PLAYERS", "0");
            setProperty("CHANNEL_STATUS", "0");
            setProperty("command_prefix", "//");
        }};
    }

    public static Properties getProps(String name, Properties defaultProps) {
        if (defaultProps == null) defaultProps = new Properties();

        File propsFile = new File(DIR_PROPS, name + ".properties");
        if (!propsFile.exists()) {
            try (FileOutputStream os = new FileOutputStream(propsFile)) {
                defaultProps.store(os, PROPS_COMMENT);
            } catch (IOException e) {
                throw new RuntimeException("Cannot store properties", e);
            }
            return defaultProps;
        }

        try (FileInputStream is = new FileInputStream(propsFile)) {
            Properties result = new Properties(defaultProps);
            result.load(is);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties", e);
        }
    }

    static void storeProps(String name, Properties properties) {
        File propsFile = new File(DIR_PROPS, name + ".properties");
        try (FileOutputStream os = new FileOutputStream(propsFile)) {
            properties.store(os, PROPS_COMMENT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot store properties", e);
        }
    }

    public static void writeDefaultGlobalProps() {
        if (!new File(DIR_PROPS, "global.properties").exists())
            storeGlobalProps(getGlobalPropsDefaults());
    }

    public static class IdNotProvidedException extends RuntimeException {
        public IdNotProvidedException(String s) {
            super(s);
        }

        public IdNotProvidedException() {
            this("Please check global config entries with prefixes `ROLE_` and `CHANNEL_`");
        }
    }
}
