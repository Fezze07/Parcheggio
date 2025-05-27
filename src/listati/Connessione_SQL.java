package listati;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Connessione_SQL {
    private static String userName;
    private static String password;
    private static String url;
    private static final Logger logger = Logger.getLogger(Connessione_SQL.class.getName());
    private static boolean inizializzato = false;

    public static void inizializza() {
        if (inizializzato) return;
        try {
            String pathConfig = GestoreDatabase.getPathConfig("config.properties");
            try (InputStream input = new FileInputStream(pathConfig)) {
                Properties prop = new Properties();
                prop.load(input);
                String host = prop.getProperty("db.host");
                String porta = prop.getProperty("db.porta");
                String databaseName = prop.getProperty("db.dbname");
                userName = prop.getProperty("db.username");
                password = prop.getProperty("db.password");
                url = "jdbc:mysql://" + host + ":" + porta + "/" + databaseName + "?sslmode=require";
                inizializzato = true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore caricamento config", e);
        }
    }

    public static Connection creaConnessione() throws SQLException {
        inizializza();
        return DriverManager.getConnection(url, userName, password);
    }

    public static void testConnessione() {
        try (Connection conn = creaConnessione()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connessione riuscita!");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "❌ Connessione fallita", e);
        }
    }

}
