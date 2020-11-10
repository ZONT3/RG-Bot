package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.zont.rgdsb.*;

import java.awt.*;
import java.util.Properties;

import static ru.zont.rgdsb.Strings.*;

public class GMs extends LongInteractAdapter {
    public GMs() throws RegisterException {
        super();
    }

    @Override
    public void onRequestLong(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        String[] args = Commands.parseArgs(this, event);
        if (args.length < 1)
            throw new UserInvalidArgumentException(STR.getString("err.incargs"));
        switch (args[0].toLowerCase()) {
            case "set":
                checkArgs(args, 3);
                set(event.getGuild(), getId(args[1]), args[2]);
                ok(event);
                break;
            case "rm":
                checkArgs(args, 2);
                GameMasters.removeGm(getId(args[1]));
                ok(event);
                break;
            case "get":
                event.getChannel().sendMessage(GameMasters.retrieveGmsEmbed(event.getGuild())).queue();
                break;
        }
    }

    private void ok(@NotNull MessageReceivedEvent event) {
        event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription(":white_check_mark:")
                .build()).queue();
    }

    private long getId(String raw) {
        return Long.parseLong(raw.substring(3, raw.length() - 1));
    }

    private void set(Guild guild, long id, String steamid64) {
        Member member = guild.getMemberById(id);
        GameMasters.GM gm = new GameMasters.GM();
        gm.steamid64 = steamid64;
        gm.userid = id;
        gm.armaname = GameMasters.getArmaName(steamid64);
        gm.dsname = member != null ? member.getEffectiveName() : "<null>";
        GameMasters.setGm(gm);
    }

    private static void checkArgs(String[] args, int needed) {
        if (args.length < needed)
            throw new UserInvalidArgumentException(STR.getString("err.incargs"));
        if (!args[1].matches("<@!\\d+>"))
            throw new UserInvalidArgumentException(STR.getString("comm.gms.err.secarg"));
    }

    @Override
    public String getCommandName() {
        return "gm";
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getExample() {
        return "gm (set|get|rm) [@user steamid64]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.gms.desc");
    }

    @Override
    public boolean checkPermission(@Nullable Member member) {
        if (member == null) return false;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        String gmmID = PropertiesTools.getRoleGmmID();
        Role gmm = member.getGuild().getRoleById(gmmID);
        if (gmm == null) throw new RuntimeException("Role GMM not provided");
        for (Role role: member.getRoles())
            if (role.getId().equals(gmmID) || role.canInteract(gmm))
                return true;
        return false;
    }
}
