package ru.zont.rgdsb;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

import static ru.zont.rgdsb.Main.CHANNEL_PLAYERS;
import static ru.zont.rgdsb.Main.ROLE_PLAYER;

public class LPlayersMonitoring extends ListenerAdapter {
    private boolean ready = false;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        for (Guild guild: event.getJDA().getGuilds())
            displayPlayersTotal(guild);
        ready = true;
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        if (!ready) return;
        event.getGuild().loadMembers().onSuccess(members -> {
            for (Role role: event.getRoles())
                if (role.getIdLong() == ROLE_PLAYER) {
                    displayPlayersTotal(role.getGuild());
                    return;
                }
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!ready) return;
        for (Role role: event.getRoles())
            if (role.getIdLong() == ROLE_PLAYER) {
                displayPlayersTotal(role.getGuild());
                return;
            }
    }

    private static void displayPlayersTotal(Guild guild) {
        displayMembersWithRole(guild, guild.getRoleById(ROLE_PLAYER), CHANNEL_PLAYERS, "\ud83c\udf05 \u0418\u0433\u0440\u043e\u043a\u043e\u0432 \u0432\u0441\u0435\u0433\u043e: %d");
    }

    private static void displayMembersWithRole(Guild guild, Role role, long channelId, String formattedDisplay) {
        VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) return;

        int size = guild.getMembersWithRoles(role).size();
        System.out.printf("Updating players: %d\n", size);
        channel
                .getManager()
                .setName( String.format(formattedDisplay, size) )
                .queue(v -> System.out.println("Queued"));
    }
}
