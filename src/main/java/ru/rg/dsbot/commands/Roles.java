package ru.rg.dsbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.zont.dsbot2.NotImplementedException;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.Commons;

import java.util.*;

import static ru.rg.dsbot.tools.TRoles.*;
import static ru.zont.dsbot2.tools.ZDSBStrings.*;

public class Roles extends CommandAdapter {

    public Roles(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {
        final MessageReceivedEvent event = input.getEvent();
        Commons.rolesLikeRouter(0, this::set, this::rm, this::get)
                .setError(STR.getString("comms.gms.err.first_arg"))
                .addCase((i) ->
                        Commons.rolesLikeRouter(1, this::autoSet, this::autoRm, this::autoGet)
                                .acceptInput(i), "auto")
                .acceptInput(input);
    }

    private int parseID(String arg) {
        if (!arg.matches("[+-]?\\d+"))
            throw new UserInvalidInputException("ID should be an integer number");
        return Integer.parseInt(arg);
    }

    private ArrayList<Profile> fetchProfiles(int id) {
        final ArrayList<Profile> profiles = fetchProfilesWithRoles();
        profiles.removeIf(profile -> !profile.roles.contains(id));
        return profiles;
    }

    private Profile fetchProfile(String argDisID, String argSteamID) {
        long userid = argDisID != null ? Commons.userMentionToID(argDisID) : -1;

        Profile profile;
        String uid;
        if (argSteamID == null) {
            profile = getProfileByDisID(userid);
            uid = profile.uid;
        } else {
            uid = Commons.assertSteamID(argSteamID);
            profile = getProfileByUID(uid);
        }

        if (profile != null && userid < 0) {
            userid = profile.userid;
            if (userid == 0) userid = -1;
        }

        final HashSet<Integer> roles = profile != null ? profile.roles : new HashSet<>();
        return new Profile(uid, userid, roles);
    }

    private void set(Input input) {
        input.assertArgCount(3);
        final List<String> args = Arrays.asList(input.getArgs());
        int id = parseID(args.get(1));
        final Profile profile = fetchProfile(args.get(2), args.size() >= 4 ? args.get(3) : null);
        profile.roles.add(id);
        commitRoles(profile.roles, profile.uid, profile.userid, id, "add");
        msgDescribeUpdate(profile, input.getChannel());
    }

    private void rm(Input input) {
        input.assertArgCount(3);
        List<String> args = Arrays.asList(input.getArgs());
        int id = parseID(args.get(1));

        final String arg = args.get(2);
        long userid = -1; String steamid = null;
        try { userid = Commons.userMentionToID(arg); }
        catch (UserInvalidInputException ignored) { }
        if (userid < 0) {
            try {
                steamid = Commons.assertSteamID(arg);
            } catch (UserInvalidInputException ignored) { }
            if (steamid == null)
                throw new UserInvalidInputException("Hasn't detected steamid nor discord @mention");
        }

        final Profile profile = fetchProfile(userid < 0 ? null : arg + "", steamid);
        profile.roles.remove(id);
        commitRoles(profile.roles, profile.uid, profile.userid, id, "rm");
        msgDescribeUpdate(profile, input.getChannel());
    }

    private void get(Input input) {
        List<String> args = Arrays.asList(input.getArgs());
        if (args.size() < 2) {
            input.getChannel().sendMessage(msgList()).queue();
            return;
        }
        final String arg = args.get(1);
        long userid = -1; String steamid = null; int id = 0;
        try { userid = Commons.userMentionToID(arg); }
        catch (UserInvalidInputException ignored) { }
        if (userid < 0) {
            try {
                steamid = Commons.assertSteamID(arg);
            } catch (UserInvalidInputException ignored) { }
            try {
                id = parseID(arg);
            } catch (Throwable e) {
                throw new UserInvalidInputException("Hasn't detected steamid nor discord @mention nor role ID");
            }
        }

        if (id != 0) {
            input.getChannel().sendMessage(msgListByID(fetchProfiles(id), id)).queue();
        } else {
            final Profile profile = fetchProfile(userid < 0 ? null : arg, steamid);
            msgDescribeUpdate(profile, input.getChannel(), STR.getString("comms.roles.updated.title.d"));
        }
    }

    private void autoSet(Input input) {
        throw new NotImplementedException();
    }

    private void autoGet(Input input) {
        throw new NotImplementedException();
    }

    private void autoRm(Input input) {
        throw new NotImplementedException();
    }

    @Override
    public String getCommandName() {
        return "roles";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("role");
    }

    @Override
    public String getSynopsis() {
        return "roles add|set|get|list|rm|del|auto ...\n" +
                "roles add|set <ID> <@who> [steamID64]\n" +
                "roles rm|del <ID> <@who|steamID64>\n" +
                "roles get|list [ID|@who]\n" +
                "roles auto add|set|rm|del|list|get ...\n" +
                "roles auto add|set <@ds-role|ds-role-id> <ID>\n" +
                "roles auto rm|del <ID|@ds-role|ds-role-id>\n" +
                "roles auto list|get";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.roles.desc");
    }


    @Override
    public boolean checkPermission(Input input) {
        return Commons.rolesLikePermissions(input, Arrays.asList("set", "rm", "add", "del", "auto"));
    }

    @Override
    public boolean allowPM() {
        return false;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }
}
