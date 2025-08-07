package life.pharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:life_pharmacy.db";
    private static boolean initialized = false;

    // Charge le driver une seule fois
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne une connexion SQLite avec busy_timeout et WAL activé
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        // Configure busy_timeout pour éviter les erreurs "database is locked"
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout = 5000"); // attend 5 secondes
            if (!initialized) {
                stmt.execute("PRAGMA journal_mode = WAL"); // active WAL
                initialized = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn;
    }
}
