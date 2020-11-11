package ru.zont.rgdsb;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static ru.zont.rgdsb.Strings.STR;

public class GameMasters {
    public static ArrayList<GM> retrieve() {
        ArrayList<GM> res = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery(
                    "SELECT * FROM game_masters"
            );
            while (resultSet.next()) {
                GM gm = new GM();
                gm.steamid64 = resultSet.getString("id_steam64");
                gm.steamid64 = gm.steamid64.substring(1, gm.steamid64.length() - 1);
                gm.userid = resultSet.getLong("id_discord");
                gm.dsname = resultSet.getString("name_dis");
                gm.armaname = resultSet.getString("name_arma");
                res.add(gm);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public static void setGm(GM gm) {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            st.executeUpdate(
                    "INSERT INTO game_masters\n" +
                            "(name_dis, id_steam64, name_arma, id_discord)\n" +
                            "VALUES('"+gm.dsname+"','\""+gm.steamid64+"\"','"+gm.armaname+"',"+gm.userid+")\n" +
                        "ON DUPLICATE KEY UPDATE\n" +
                            "name_dis='"+gm.dsname+"',\n" +
                            "id_steam64='\""+gm.steamid64+"\"',\n" +
                            "name_arma='"+gm.armaname+"'\n"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeGm(long id) {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM game_masters WHERE id_discord="+id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getArmaName(String steamid64) {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery(
                    "SELECT p_name " +
                        "FROM profiles_presistent " +
                        "WHERE p_guid='\""+steamid64+"\"' " +
                        "LIMIT 1"
            );
            if (!resultSet.next())
                return "<null>";
            return resultSet.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Date getLastLogin(String steamid) {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery(
                    "SELECT p_lastupd " +
                        "FROM profiles_presistent " +
                        "WHERE p_guid='\"" + steamid + "\"' " +
                        "LIMIT 1"
            );
            if (!resultSet.next())
                return null;
            Timestamp timestamp = resultSet.getTimestamp(1);
            if (timestamp == null) return null;
            return new Date(timestamp.toInstant().getEpochSecond() * 1000);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Date getAssignedDate(long userid) {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery(
                    "SELECT st_assigned " +
                            "FROM game_masters " +
                            "WHERE id_discord=" + userid + " " +
                            "LIMIT 1"
            );
            if (!resultSet.next())
                return null;
            Timestamp timestamp = resultSet.getTimestamp(1);
            if (timestamp == null) return null;
            return new Date(timestamp.toInstant().getEpochSecond() * 1000);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MessageEmbed retrieveGmsEmbed(List<GM> gms, Guild guild) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(STR.getString("comm.gms.get.title"));
        for (GM gm: gms) {
            Member member = guild.getMemberById(gm.userid);
            String memberStr = member != null ? member.getAsMention() : STR.getString("comm.gms.get.unknown");
            Date lastLogin = getLastLogin(gm.steamid64);
            Date dateAssigned = getAssignedDate(gm.userid);

            String online, assigned;
            boolean bold = false;

            if (lastLogin != null) {
                long diff = (System.currentTimeMillis() - lastLogin.getTime()) / 1000 / 60;
                if (diff > 1) {
                    bold = (diff / 60 / 24) > 0;
                    String was = String.format(STR.getString("comm.gms.get.lastlogin.d"), diff / 60 / 24, diff / 60 % 24);
                    if (diff < 60) was = STR.getString("comm.gms.get.lastlogin.j");
                    online = String.format(STR.getString("comm.gms.get.lastlogin"), was);
                } else online = STR.getString("comm.gms.get.lastlogin.n");
            } else online = STR.getString("comm.gms.get.lastlogin.unk");

            assigned = String.format(STR.getString("comm.gms.get.assigned"), (
                    dateAssigned != null
                    ? new SimpleDateFormat("dd.MM.yyyy HH:mm").format(dateAssigned)
                    : STR.getString("comm.gms.get.assigned.l") ));

            String armaName = String.format(STR.getString("comm.gms.get.armaname"), getArmaName(gm.steamid64));
            builder.appendDescription(field(memberStr, armaName, assigned, online, bold));
        }
        return builder.setColor(0x9900ff).build();
    }

    private static String field(String memberStr, String armaName, String assigned, String string, boolean bold) {
        return String.format(bold ? "%s\n%s\n%s\n**%s**\n\n" : "%s\n%s\n%s\n%s\n\n", memberStr, armaName, assigned, string);
    }

    public static class GM {
        public String steamid64;
        public long userid;
        public String dsname;
        public String armaname;
    }
}
