package ru.zont.rgdsb;

import java.sql.*;

public class ServerInfoWatch extends Thread {
    private Callback callback;
    private final long period;
    private boolean once = false;

    public ServerInfoWatch(long period) {
        this.period = period;
    }

    public ServerInfoWatch() {
        period = 1;
        once = true;
    }

    @Override
    public void run() {
        String connectionUrl =
                "jdbc:mariadb://185.189.255.57:3306/arma?user=rgbot&password=43092";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
             Statement st = connection.createStatement()) {
            while (!interrupted()) {
                ResultSet a = st.executeQuery(
                        "SELECT\n" +
                            "    s_players_count,\n" +
                            "    s_servertime,\n" +
                            "    s_updated,\n" +
                            "    s_gms,\n" +
                            "    s_players_record\n" +
                            "FROM server_info\n" +
                            "WHERE s_port = 2302\n" +
                            "LIMIT 1" );
                if (!a.next()) throw new NoResponseException();

                ResultSet b = st.executeQuery("SELECT CURRENT_TIMESTAMP()");
                if (!b.next()) throw new NoResponseException();
                long currentTime = b.getTimestamp(1).getTime();

                ResultSet c = st.executeQuery("SELECT COUNT(*) FROM profiles_presistent");
                if (!c.next()) throw new NoResponseException();

                short record = a.getShort("s_players_record");
                short count = a.getShort("s_players_count");
                long updated = a.getTimestamp("s_updated").getTime();
                long servertime = a.getLong("s_servertime");
                String gms = a.getString("s_gms");
                int total = c.getInt(1);

                if (record < count) commitRecord(st, record);

                callback(
                        count,
                        currentTime - updated,
                        servertime,
                        gms,
                        record,
                        total );

                if (!once)
                    sleep(period);
                else break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException ignored) { }
    }

    private void commitRecord(Statement st, short record) throws SQLException {
        st.executeUpdate(
                "UPDATE server_info\n" +
                "    SET s_players_record = "+ record + "\n" +
                "    WHERE s_port = 2302\n" +
                "    LIMIT 1");
    }

    private void callback(short count, long time, long uptime, String gms, short record, int total) {
        if (callback != null)
            callback.onUpdate(count, time, uptime, gms, record, total);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onUpdate(short count, long time, long uptime, String gms, short record, int total);
    }

    private static class NoResponseException extends SQLException {
        public NoResponseException() {
            super("No response from DB");
        }
    }
}
