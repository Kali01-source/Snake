package Managers;

import Misc.PlayerScore;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager implements AutoCloseable {

    private static final String JDBC_URL = "jdbc:sqlite:highscores.db";

    private final int maxScores;
    private final Connection connection;
    private final PreparedStatement insertStatement;
    private final PreparedStatement deleteByScoreStatement;

    public DatabaseManager(int maxScores) throws SQLException {
        this.maxScores = maxScores;

        this.connection = DriverManager.getConnection(JDBC_URL);

        try (Statement st = connection.createStatement()) {
            st.execute(
                    "CREATE TABLE IF NOT EXISTS HIGHSCORES (" +
                            "  TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "  NAME      VARCHAR(255) NOT NULL," +
                            "  SCORE     INT NOT NULL" +
                            ")"
            );
        }

        this.insertStatement = connection.prepareStatement(
                "INSERT INTO HIGHSCORES (TIMESTAMP, NAME, SCORE) VALUES (?, ?, ?)"
        );
        this.deleteByScoreStatement = connection.prepareStatement(
                "DELETE FROM HIGHSCORES WHERE SCORE=?"
        );
    }

    private void insertScore(String name, int score) throws SQLException {
        insertStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        insertStatement.setString(2, name);
        insertStatement.setInt(3, score);
        insertStatement.executeUpdate();
    }

    private void deleteScores(int score) throws SQLException {
        deleteByScoreStatement.setInt(1, score);
        deleteByScoreStatement.executeUpdate();
    }

    private void sortDesc(ArrayList<PlayerScore> list) {
        list.sort((a, b) -> b.score() - a.score());
    }

    public void saveScore(PlayerScore ps) throws SQLException {
        ArrayList<PlayerScore> current = listAllScores();
        sortDesc(current);

        if (current.size() < maxScores) {
            insertScore(ps.playerName(), ps.score());
        } else {
            int least = current.getLast().score();
            if (ps.score() > least) {
                deleteScores(least);
                insertScore(ps.playerName(), ps.score());
            }
        }
    }

    private ArrayList<PlayerScore> listAllScores() throws SQLException {
        ArrayList<PlayerScore> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT NAME, SCORE FROM HIGHSCORES")) {
            while (rs.next()) {
                list.add(new PlayerScore(rs.getString("NAME"), rs.getInt("SCORE")));
            }
        }
        return list;
    }

    public ArrayList<PlayerScore> getTopScores(int limit) throws SQLException {
        ArrayList<PlayerScore> all = listAllScores();
        sortDesc(all);
        if (limit >= all.size()) return all;
        return new ArrayList<>(all.subList(0, limit));
    }

    @Override public void close() {
        try { insertStatement.close(); } catch (Exception ignored) {}
        try { deleteByScoreStatement.close(); } catch (Exception ignored) {}
        try { connection.close(); } catch (Exception ignored) {}
    }
}