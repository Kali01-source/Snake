import Managers.GameManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            new GameManager();
        }
        catch (SQLException e) {
            System.err.println("Error while connecting to database");
            System.exit(0);
        }
    }
}
