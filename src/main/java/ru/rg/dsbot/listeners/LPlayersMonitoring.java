package ru.rg.dsbot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.rg.dsbot.Strings;

import javax.annotation.Nonnull;

public class LPlayersMonitoring extends ListenerAdapter {
    public static final String ID_PLAYER = "747533854625235024";
    public static final String ID_CHANNEL = "765683007046287360";
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
                if (role.getId().equals(ID_PLAYER)) { // TODO instantiate
                    displayPlayersTotal(role.getGuild());
                    return;
                }
        });
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!ready) return;
        for (Role role: event.getRoles())
            if (role.getId().equals(ID_PLAYER)) {
                displayPlayersTotal(role.getGuild());
                return;
            }
    }

    private static void displayPlayersTotal(Guild guild) {
        displayMembersWithRole(guild, guild.getRoleById(ID_PLAYER), ID_CHANNEL, Strings.STR.getString("player_monitoring.players"));
    }

    private static void displayMembersWithRole(Guild guild, Role role, String channelId, String formattedDisplay) {
        VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) return;

        int size = guild.getMembersWithRoles(role).size();
        channel
                .getManager()
                .setName( String.format(formattedDisplay, size) )
                .queue();
    }
}
