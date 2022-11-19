package com.rft.tone.db;

import com.rft.tone.App;
import com.rft.tone.cmdsrv.Command;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Log4j2
public class SqlLiteCommandDb implements AutoCloseable {

    private static SqlLiteCommandDb instance;
    public static SqlLiteCommandDb getInstance(String dbName) throws SQLException {
        if (instance == null) {
            instance = new SqlLiteCommandDb();
            instance.initialize(dbName);
        }
        return instance;
    }

    public long lastSlotId = 0L;
    public long lastTermId = 0L;
    public long lastCommittedSlotId = 0L;

    /**
     * LogCommands
     * SlotId Unique
     * TermId,
     * Command
     */
    private SqlLiteCommandDb() {
    }

    public boolean appendEntries(Command entry, ArrayList<Command> entries) throws SQLException {
        Command firstEntry = entries.get(0);
        if (firstEntry.getSlotId() == lastSlotId && firstEntry.getTermId() == lastTermId) {
            for (Command eachRemainEntry : entries.stream().skip(0).collect(Collectors.toList())) {
                this.insertEntry(eachRemainEntry);
            }

            this.insertEntry(entry);
            return true;
        } else {
            return false;
        }
    }

    private void insertEntry(Command eachRemainEntry) throws SQLException {
        String query = "" +
                "INSERT OR REPLACE INTO LogCommands(slot_id, term_id, command, is_committed) " +
                "VALUES (1, 1, 'cmd', 0);";

        connection
                .createStatement()
                .executeUpdate(
                        String.format(
                                query,
                                eachRemainEntry.getSlotId(),
                                eachRemainEntry.getTermId(),
                                eachRemainEntry.getCommand(),
                                eachRemainEntry.isCommitted()));
    }

    private Connection connection;
    public void initialize(String dbName) throws SQLException {
        try {
            // create a database connection
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:build/%s.dbx", dbName) );
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(App.MIN_TIMER_SEC);
            statement.executeUpdate("" +
                    "CREATE TABLE if not exists LogCommands(" +
                    "slot_id INTEGER NOT NULL PRIMARY KEY," +
                    "term_id INTEGER NOT NULL," +
                    "command TEXT NOT NULL," +
                    "is_committed INTEGER NOT NULL" +
                    ");");

            ResultSet rs = statement.executeQuery("SELECT MAX(slot_id) as slot_id from LogCommands");

            while (rs.next()) {
                this.lastSlotId = rs.getLong("slot_id");
            }

            if(this.lastSlotId != 0) {
                ResultSet rs2 = statement.executeQuery("SELECT slot_id, term_id as slot_id from LogCommands WHERE slot_id = " + this.lastSlotId);
                while (rs2.next()) {
                    this.lastTermId = rs2.getLong("term_id");
                }
            }

            ResultSet rs3 = statement.executeQuery("SELECT MAX(slot_id) as slot_id from LogCommands WHERE is_committed = 1 AND slot_id = " + this.lastSlotId);
            while (rs3.next()) {
                this.lastCommittedSlotId = rs3.getLong("slot_id");
            }

            log.info("[LogEntry]: lastSlotId: {} lastTermId: {} lastCommittedSlotId: {}", lastSlotId, lastTermId, lastCommittedSlotId);
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            log.error(e);
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null)
            connection.close();
        System.out.println("CLOSED");
    }
}
