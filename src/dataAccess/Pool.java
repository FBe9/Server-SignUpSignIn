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
public class Pool {

    private ResourceBundle configFile;
    private String db_user;
    private String db_pass;
    private static Stack<Connection> connections = new Stack<>();
    private static Pool pool;
    private static final Logger logger = Logger.getLogger(Pool.class.getName());

    /**
     * Constructs a new connection pool and initializes it.
     */
    public Pool() {

        configFile = ResourceBundle.getBundle("config.config");
        db_user = configFile.getString("DB_USER");
        db_pass = configFile.getString("DB_PASSWORD");

    }

    /**
     * Retrieves the singleton instance of the Pool class.
     *
     * This method returns the existing Pool instance if it has already been
     * created, or it creates a new Pool instance if one does not exist.
     *
     * @return The singleton instance of the Pool class.
     */
    public synchronized static Pool getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }

    /**
     * Starts and connects an SSH session to connect to the remote DB server.
     * Retrieves a database connection from the pool. If the pool is empty, a
     * new connection is created.
     *
     * @return A database connection.
     * @throws exceptions.ServerErrorException
     */
    public synchronized Connection takeConnection() throws ServerErrorException {
        Connection con = null;

        //Checks if the stack is empty
        if (connections.isEmpty()) {
            try {
                // DB URL with assigned SSH port
                String url = configFile.getString("URL");
                //Creates a new connection
                con = DriverManager.getConnection(url, db_user, db_pass);
            } catch (SQLException ex) {
                throw new ServerErrorException();
            }
        } else {
            //Gets the connection from the stack
            con = connections.pop();
        }
        //Returns the connection
        return con;
    }

    /**
     * Returns a database connection to the pool for reuse.
     *
     * @param con The database connection to be returned to the pool.
     */
    public synchronized void returnConnection(Connection con) {
        //Checks if connection received is null

        if (con != null) {
            //Adds the connection to the stack
            connections.push(con);
        }
    }

    /**
     * Closes all connections in the pool and clears the pool. Disconnects the
     * SSH session.
     */
    public void closeAllConnections() {
        //Message for the user
        logger.info("Closing all the connections of the Pool");
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
