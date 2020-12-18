package ru.zont.rgdsb.command.exec;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.commands.Commands;
import ru.zont.rgdsb.tools.Configs;

import java.io.File;
import java.util.Properties;

import static ru.zont.dsbot.core.commands.Commands.Input;
import static ru.zont.dsbot.core.commands.Commands.parseInput;
import static ru.zont.dsbot.core.tools.Strings.STR;

public class Do extends CommandAdapter {
    public Do(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        final Input input = parseInput(this, event);
        String name = input.getArg(0);
        if (!name.matches("[\\w\\-.]+"))
            throw new UserInvalidArgumentException(STR.getString("comm.do.err.name"));
        if (name.endsWith(".py"))
            name = name.substring(0, name.length() - 3);

        Commands.call(Exec.class,
                String.format("-V \"--name=%s\" python -X utf8 -u %s", name, resolveScript(name) + input.stripPrefixOpts().replaceFirst(name, "")),
                event, getBot());
    }

    private String resolveScript(String raw) {
        File main = new File("scripts", raw + ".py");
        if (!main.exists()) throw new UserInvalidArgumentException(STR.getString("comm.do.err.name"));
        return main.getAbsolutePath();
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        final Member member = event.getMember();
        if (member == null) return false;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;

        Role mapper = event.getGuild().getRoleById(Configs.getRoleMapperID());
        if (mapper != null)
            for (Role role: member.getRoles())
                if (role.getPosition() >= mapper.getPosition())
                    return true;

        return false;
    }

    @Override
    public String getCommandName() {
        return "do";
    }

    @Override
    public String getSynopsis() {
        return "do <script_name>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.do.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }
}
