package ru.zont.rgdsb;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {
    static final long ROLE_PLAYER     = 747533854625235024L;
    static final long CHANNEL_PLAYERS = 765683007046287360L;
    static final long CHANNEL_STATUS  = 766376696974147665L;

    public static void main(String[] args) throws LoginException, InterruptedException {
        if (args.length == 0) throw new LoginException("API token not provided!");
        JDA bot = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new LPlayersMonitoring(), new LServerState())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build().awaitReady();
    }
}
