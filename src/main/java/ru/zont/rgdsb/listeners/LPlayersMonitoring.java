package ru.zont.rgdsb.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.zont.rgdsb.LOG;
import ru.zont.rgdsb.PropertiesTools;
import ru.zont.rgdsb.Strings;

import javax.annotation.Nonnull;

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
                if (role.getId().equals(PropertiesTools.getRolePlayerID())) {
                    displayPlayersTotal(role.getGuild());
                    return;
                }
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!ready) return;
        for (Role role: event.getRoles())
            if (role.getId().equals(PropertiesTools.getRolePlayerID())) {
                displayPlayersTotal(role.getGuild());
                return;
            }
    }

    private static void displayPlayersTotal(Guild guild) {
        displayMembersWithRole(guild, guild.getRoleById(PropertiesTools.getRolePlayerID()), PropertiesTools.getChannelPlayersID(), Strings.STR.getString("plmon.players"));
    }

    private static void displayMembersWithRole(Guild guild, Role role, String channelId, String formattedDisplay) {
        VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) return;

        int size = guild.getMembersWithRoles(role).size();
        LOG.d("Updating players: %d", size);
        channel
                .getManager()
                .setName( String.format(formattedDisplay, size) )
                .queue(v -> LOG.d("Queued"));
    }
}