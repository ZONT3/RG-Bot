package ru.zont.rgdsb;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

import static ru.zont.rgdsb.Main.*;

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
                if (role.getId().equals(PropertiesTools.getPlayerRoleID())) {
                    displayPlayersTotal(role.getGuild());
                    return;
                }
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!ready) return;
        for (Role role: event.getRoles())
            if (role.getId().equals(PropertiesTools.getPlayerRoleID())) {
                displayPlayersTotal(role.getGuild());
                return;
            }
    }

    private static void displayPlayersTotal(Guild guild) {
        displayMembersWithRole(guild, guild.getRoleById(PropertiesTools.getPlayerRoleID()), PropertiesTools.getChannelPlayersID(), STR.getString("plmon.players"));
    }

    private static void displayMembersWithRole(Guild guild, Role role, String channelId, String formattedDisplay) {
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
