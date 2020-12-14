package ru.zont.rgdsb.listeners;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import ru.zont.dsbot.core.Configs;
import ru.zont.dsbot.core.Strings;
import ru.zont.rgdsb.tools.Globals;
import ru.zont.rgdsb.tools.messages.Status;

import java.sql.*;
import java.util.List;

import static ru.zont.rgdsb.tools.Configs.*;

public class StatusMain extends ServerStatusEntry {

    @Override
    MessageEmbed getInitialMsg() {
        return Status.serverInactive();
    }

    @Override
    void update(Message entryMessage) {
        ServerInfoStruct struct = retrieveServerInfo();

        entryMessage.getJDA().getPresence().setActivity(
                Activity.watching(
                        String.format(Strings.STR.getString("status.main.status"),
                                struct.count,
                                Configs.getPrefix() )));

        Role roleGM = entryMessage.getGuild().getRoleById(getRoleGmID());
        entryMessage.editMessage(Status.status(struct, roleGM != null ? roleGM.getAsMention() : "GM")).queue();
    }

    public static ServerInfoStruct retrieveServerInfo() {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet a = st.executeQuery(
                    "SELECT\n" +
                        "    s_players_count,\n" +
                        "    s_servertime,\n" +
                        "    s_updated,\n" +
                        "    s_gms,\n" +
                        "    s_players_record\n" +
                        "FROM server_info\n" +
                        "WHERE s_port = 2302\n" +
                        "LIMIT 1");
            if (!a.next()) throw new NoResponseException();

            ResultSet b = st.executeQuery("SELECT CURRENT_TIMESTAMP()");
            if (!b.next()) throw new NoResponseException();
            long currentTime = b.getTimestamp(1).getTime();

            short record = a.getShort("s_players_record");
            short count = a.getShort("s_players_count");
            long updated = a.getTimestamp("s_updated").getTime();
            long servertime = a.getLong("s_servertime");
            String gms = a.getString("s_gms").replaceAll("\"\"", "");

            if (record < count) commitRecord(st, record);

            return new ServerInfoStruct(
                    count,
                    currentTime - updated,
                    servertime,
                    jsonList(gms),
                    record );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void commitRecord(Statement st, short record) throws SQLException {
        st.executeUpdate(
                "UPDATE server_info\n" +
                        "    SET s_players_record="+ record + "\n" +
                        "    WHERE s_port = 2302\n" +
                        "    LIMIT 1");
    }

    private static List<String> jsonList(String gms) {
        return new Gson().fromJson(gms, new TypeToken<List<String>>() {}.getType());
    }

    public static class ServerInfoStruct {
        public short count;
        public long time;
        public long uptime;
        public List<String> gms;
        public short record;
        public int total;

        public ServerInfoStruct(short count, long time, long uptime, List<String> gms, short record) {
            this.count = count;
            this.time = time;
            this.uptime = uptime;
            this.gms = gms;
            this.record = record;
        }
    }

}
