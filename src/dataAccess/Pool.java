package dataAccess;

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
 */
public class Pool {

    private ResourceBundle configFile;
    private String url;
    private String user;
    private String pass;
    Stack<Connection> connections = new Stack<>();
    private static final Logger logger = Logger.getLogger(Pool.class.getName());

    /**
     * Constructs a new connection pool and initializes it.
     */
    public Pool() {

        configFile = ResourceBundle.getBundle("service.Config");
        url = configFile.getString("URL");
        user = configFile.getString("USER");
        pass = configFile.getString("PASSWORD");
    }

    /**
     * Retrieves a database connection from the pool. If the pool is empty, a
     * new connection is created.
     *
     * @return A database connection.
     */

    public Connection takeConnection() {
        Connection con = null;
        //Checks if the stack is empty
        if (connections.isEmpty()) {
            try {
                //Creates a new connection
                con = DriverManager.getConnection(url, user, pass);
            } catch (SQLException ex) {
                Logger.getLogger(Pool.class.getName()).log(Level.SEVERE, null, ex);
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
    public void returnConnection(Connection con) {
        //Checks if connection received is null
        if (con != null) {
            //Adds the connection to the stack
            connections.push(con);
        }

    }

    /**
     * Closes all connections in the pool and clears the pool.
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
