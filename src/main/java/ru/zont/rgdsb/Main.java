package ru.zont.rgdsb;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
import ru.zont.rgdsb.command.CommandAdapter;
import ru.zont.rgdsb.listeners.LPlayersMonitoring;
import ru.zont.rgdsb.listeners.LServerStatus;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Modifier;
import java.util.*;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, InterruptedException {
        Globals.commandAdapters = registerInteracts();

        PropertiesTools.writeDefaultGlobalProps();

        if (args.length < 2) throw new LoginException("API token and/or DB connection not provided!");
        Globals.dbConnection = args[1];

        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(
                        new Main(),
                        Globals.playersMonitoring = new LPlayersMonitoring(),
                        Globals.serverStatus = new LServerStatus()
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build().awaitReady();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        try {
            CommandAdapter.onMessageReceived(event, Globals.commandAdapters);
        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(
                    new EmbedBuilder( Messages.error(
                            Strings.STR.getString("err.unexpected"),
                            String.format("%s: %s", e.getClass().getName(), e.getLocalizedMessage())))
                    .setFooter(Strings.STR.getString("err.unexpected.foot"))
                    .build()).queue();
        }
    }

    private static CommandAdapter[] registerInteracts() {
        if (PropertiesTools.DIR_PROPS.exists() && !PropertiesTools.DIR_PROPS.isDirectory())
            if (!PropertiesTools.DIR_PROPS.delete())
                throw new RuntimeException("Cannot remove file named as dir 'properties'");
        if (!PropertiesTools.DIR_PROPS.exists())
            if (!PropertiesTools.DIR_PROPS.mkdir())
                throw new RuntimeException("Cannot create properties dir");

        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("ru.zont.rgdsb.interact"))));
        Set<Class<? extends CommandAdapter>> allClasses =
                reflections.getSubTypesOf(CommandAdapter.class);

        ArrayList<CommandAdapter> res = new ArrayList<>();
        for (Class<? extends CommandAdapter> klass: allClasses) {
            if (Modifier.isAbstract(klass.getModifiers())) continue;
            try {
                System.out.printf("Registering InteractAdapter class: %s\n", klass.getSimpleName());
                CommandAdapter adapter = klass.newInstance();
                res.add(adapter);
                System.out.printf("Successfully registered adapter #%d, commandName: %s\n", res.size(), adapter.getCommandName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res.toArray(new CommandAdapter[0]);
    }
}
