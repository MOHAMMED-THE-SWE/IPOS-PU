package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties props = new Properties();

    // this block of code runce once automatically when the class if first used 
    // so for example when any code calls databaseconfig.getconnection() for the first time,
    //java loads this class and this block runs first
    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }
    
    // Read values from props (loaded from config.properties)
    public static Connection getConnection() throws SQLException {
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");
        // Ask JDBC to connect to MySQL using those details.
        return DriverManager.getConnection(url, user, pass);
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}
