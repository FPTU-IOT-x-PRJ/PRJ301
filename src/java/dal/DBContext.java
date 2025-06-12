package dal;

import utils.ConfigManager;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBContext {
    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());
    protected Connection connection;
    private ConfigManager configManager;
    
    public DBContext() {
        // Get ConfigManager instance
        configManager = ConfigManager.getInstance();
        initializeConnection();
    }
    
    /**
     * Initialize connection from configuration
     */
    private void initializeConnection() {
        try {
            // Get information from ConfigManager
            String url = configManager.getProperty("db.url");
            String username = configManager.getProperty("db.username");
            String password = configManager.getProperty("db.password");
            
            // Check configuration information
            if (url == null || username == null || password == null) {
                throw new RuntimeException("Missing database configuration information in .env file");
            }
            
            // Load driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // Create connection
            connection = DriverManager.getConnection(url, username, password);
            LOGGER.log(Level.INFO, "Database connection established successfully");
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQL Server JDBC Driver not found", e);
            throw new RuntimeException("SQL Server JDBC Driver does not exist", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to database", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
    
    /**
     * Get current connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still active
            if (connection == null || connection.isClosed()) {
                LOGGER.log(Level.WARNING, "Connection is closed, creating new connection");
                initializeConnection();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking connection, creating new connection", e);
            initializeConnection();
        }
        return connection;
    }
}
