package ru.zont.rgdsb;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main extends ListenerAdapter {
    public static final File DIR_PROPS = new File("properties");

    static final long ROLE_PLAYER     = 747533854625235024L;
    static final long CHANNEL_PLAYERS = 765683007046287360L;
    static final long CHANNEL_STATUS  = 766376696974147665L;

    public static void main(String[] args) throws LoginException, InterruptedException, InstantiationException, IllegalAccessException {
        if (args.length == 0) throw new LoginException("API token not provided!");
        JDA bot = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new LPlayersMonitoring(), new LServerState())
                .addEventListeners(registerInteracts())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build().awaitReady();
    }

    private static Object[] registerInteracts() {
        if (DIR_PROPS.exists() && !DIR_PROPS.isDirectory())
            if (!DIR_PROPS.delete())
                throw new RuntimeException("Cannot remove file named as dir 'properties'");
        if (!DIR_PROPS.exists())
            if (!DIR_PROPS.mkdir())
                throw new RuntimeException("Cannot create properties dir");

        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("ru.zont.rgdsb"))));
        Set<Class<? extends InteractAdapter>> allClasses =
                reflections.getSubTypesOf(InteractAdapter.class);

        ArrayList<Object> res = new ArrayList<>();
        for (Class<? extends InteractAdapter> klass: allClasses) {
            try {
                System.out.printf("Registering InteractAdapter class: %s\n", klass.getSimpleName());
                InteractAdapter adapter = klass.newInstance();
                res.add(adapter);
                System.out.printf("Successfully registered adapter #%d, commandName: %s\n", res.size(), adapter.getCommandName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res.toArray();
    }
}
