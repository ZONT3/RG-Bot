package ru.zont.rgdsb.listeners;

import javafx.util.Pair;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.rgdsb.tools.Globals;
import ru.zont.rgdsb.tools.messages.Status;

import java.sql.*;

public class StatusStatistics extends ServerStatusEntry {
    @Override
    MessageEmbed getInitialMsg() {
        return Status.serverStatisticInitial();
    }

    @Override
    void update(Message entryMessage) {
        Pair<Short, Short> result = retrieve();
        entryMessage.editMessage(Status.serverStatistic(result.getKey(), result.getValue())).queue();
    }

    public static Pair<Short, Short> retrieve() {
        try (Connection connection = DriverManager.getConnection(Globals.dbConnection);
             Statement st = connection.createStatement()) {
            ResultSet resultRecord = st.executeQuery("SELECT s_players_record FROM server_info WHERE s_port=2302");
            if (!resultRecord.next())
                throw new NoResponseException();
            short record = resultRecord.getShort(1);
            ResultSet resultTotal = st.executeQuery("SELECT COUNT(*) FROM profiles_presistent");
            if (!resultTotal.next())
                throw new NoResponseException();
            short total = resultTotal.getShort(1);
            return new Pair<>(record, total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
