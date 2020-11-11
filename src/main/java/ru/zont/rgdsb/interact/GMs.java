package ru.zont.rgdsb.interact;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.rgdsb.*;
import ru.zont.rgdsb.Commands.Input;

import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

import static ru.zont.rgdsb.Strings.STR;

public class GMs extends LongInteractAdapter {
    public GMs() throws RegisterException {
        super();
    }

    @Override
    public void onRequestLong(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Input input = Commands.parseInput(this, event);
        ArrayList<String> args = input.getArgs();
        if (args.size() < 1)
            throw new UserInvalidArgumentException(STR.getString("err.incargs"));
        switch (args.get(0).toLowerCase()) {
            case "set":
                checkArgs(args, 3);
                set(event.getGuild(), getId(args.get(1)), args.get(2));
                ok(event);
                break;
            case "rm":
                checkArgs(args, 2);
                GameMasters.removeGm(getId(args.get(1)));
                ok(event);
                break;
            case "list":
            case "get":
                event.getChannel().sendMessage(
                        Messages.addTimestamp(Messages.gmList(
                                GameMasters.retrieve(),
                                event.getGuild(),
                                input.hasOpt("l"),
                                input.hasOpt("s")
                        ))).queue();
                break;
            default: throw new UserInvalidArgumentException(STR.getString("comm.gms.err.firstarg"));
        }
    }

    private void ok(@NotNull MessageReceivedEvent event) {
        event.getChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription(":white_check_mark:")
                .build()).queue();
    }

    private long getId(String raw) {
        return Long.parseLong(raw.substring(raw.matches("<@\\d+>") ? 2 : 3, raw.length() - 1));
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

    private static void checkArgs(ArrayList<String> args, int needed) {
        if (args.size() < needed)
            throw new UserInvalidArgumentException(STR.getString("err.incargs"));
        if (!args.get(1).matches("<@!?\\d+>"))
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
        return "gm [-ls] (set|get|rm) [@user steamid64]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.gms.desc");
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        String[] args = Commands.parseArgs(this, event);
        if (args.length >= 2 && args[1].toLowerCase().equals("get"))
            return true;
        Member member = event.getMember();
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
