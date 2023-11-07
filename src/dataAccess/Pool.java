package dataAccess;

import exceptions.ServerErrorException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The `Pool` class represents a connection pool for managing database
 * connections.
 *
 * @author Irati
 * @author Olivia
 */
public class Pool implements PoolCreatable {

    private ResourceBundle configFile;
    private String db_user;
    private String db_pass;
    private String url;
    private static Stack<Connection> connections;
    private static final Logger LOGGER = Logger.getLogger("package dataAcess");

    /**
     * Constructs a new connection pool and initialises it.
     */
    public Pool() {
        connections = new Stack<>();
        configFile = ResourceBundle.getBundle("config.config");
        url = configFile.getString("URL");
        db_user = configFile.getString("DB_USER");
        db_pass = configFile.getString("DB_PASSWORD");

    }

    @Override
    public synchronized Connection takeConnection() throws ServerErrorException {
        Connection con = null;

        //Checks if the stack is empty
        if (connections.isEmpty()) {
            try {
                //Creates a new connection
                con = DriverManager.getConnection(url, db_user, db_pass);
                LOGGER.info("First Pool connection.");

            } catch (SQLException ex) {
                LOGGER.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
                throw new ServerErrorException(ex.getMessage());
            }
        } else {
            //Gets the connection from the stack
            LOGGER.info("Getting a pool connection from the Pool.");
            con = connections.pop();
        }
        //Returns the connection
        return con;
    }

    @Override
    public synchronized void returnConnection(Connection con) {
        //Checks if connection received is null

        if (con != null) {
            //Adds the connection to the stack
            connections.push(con);
            LOGGER.info("Returning a pool connection to the Pool.");
        }
    }

    /**
     * Closes all connections in the pool and clears the pool.
     */
    @Override
    public void closeAllConnections() {
        //Message for the user
        LOGGER.info("Closing all Pool connections.");
        //Closes the connections
        for (Connection con : connections) {
            try {
                con.close();
            } catch (SQLException e) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        //Clears the stack
        connections.clear();
    }

}
