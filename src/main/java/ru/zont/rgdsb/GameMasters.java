package ru.zont.rgdsb;

import java.sql.*;
import java.util.ArrayList;

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

    public static class GM {
        public String steamid64;
        public long userid;
        public String dsname;
        public String armaname;
    }
}
