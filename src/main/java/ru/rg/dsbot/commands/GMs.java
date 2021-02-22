package ru.rg.dsbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import ru.rg.dsbot.tools.TGameMasters;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.Commons;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.util.Arrays;
import java.util.Properties;

import static ru.rg.dsbot.Strings.*;


public class GMs extends CommandAdapter {

    public GMs(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {
        String[] args = input.getArgs();

        Options options = new Options();
        options.addOption("s", "steamid", false, "");
        options.addOption("n", "name", false, "");
        options.addOption("a", "assigned", false, "");
        options.addOption("o", "online", false, "");
        CommandLine cli = input.getCommandLine(options);

        if (args.length < 1)
            throw new UserInvalidInputException(STR.getString("err.incorrect_args"));
        switch (args[0].toLowerCase()) {
            case "set":
            case "add":
                call(Roles.class, input.getEvent(),
                    "add", "1",
                        input.getArg(1) + (input.getArg(2) != null ? " " + input.getArg(2) : ""));
                break;
            case "rm":
            case "del":
                call(Roles.class, input.getEvent(), "rm 1 " + input.getArg(1));
                break;
            case "list":
            case "get":
                ZDSBMessages.sendSplit(
                        input.getChannel(),
                        TGameMasters.Msg.gmList(
                                TGameMasters.retrieve(),
                                cli.hasOption("s"),
                                cli.hasOption("n"),
                                cli.hasOption("a"),
                                cli.hasOption("o") ),
                        true );
                break;
            default: throw new UserInvalidInputException(STR.getString("comms.gms.err.first_arg"));
        }
    }

    @Override
    public String getCommandName() {
        return "gm";
    }

    @Override
    public String getSynopsis() {
        return  "gm [-snao] set|add|get|list|rm|del ...\n" +
                "gm set|add <@user> [steamid64]\n" +
                "gm [-snao] get|list\n" +
                "gm rm|del <@user>|<steamid64>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.gms.desc");
    }

    @Override
    public boolean checkPermission(Input input) {
        return Commons.rolesLikePermissions(input, Arrays.asList("set", "rm", "add", "del"));
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
