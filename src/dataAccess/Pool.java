package dataAccess;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
    private String db_user, ssh_user;
    private String db_pass, ssh_pass;
    Stack<Connection> connections = new Stack<>();
    private static final Logger logger = Logger.getLogger(Pool.class.getName());
    Session session = null;

    /**
     * Constructs a new connection pool and initializes it.
     */
    public Pool() {

        configFile = ResourceBundle.getBundle("config.Config");
        db_user = configFile.getString("DB_USER");
        db_pass = configFile.getString("DB_PASSWORD");

    }

    /**
     * Starts and connects an SSH session to connect to the remote DB server.
     * Retrieves a database connection from the pool. If the pool is empty, a
     * new connection is created.
     *
     * @return A database connection.
     * @throws exceptions.ServerErrorException
     */
    public Connection takeConnection() throws ServerErrorException {
        Connection con = null;
        
        // For SSH sessions
        
        int assigned_port = openSSHConnection();

        //Checks if the stack is empty
        if (connections.isEmpty() && assigned_port != 0) {
            try {
                // DB URL with assigned SSH port
                StringBuilder url = new StringBuilder(configFile.getString("URL"));
                url.append(assigned_port).append(configFile.getString("DB"));
                //Creates a new connection
                con = DriverManager.getConnection(url.toString(), db_user, db_pass);
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
    public void returnConnection(Connection con) {
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
        //Close SSH session
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * Creates an SSH connection to the postgres DB.
     * 
     * @return the assigned port during the port forwarding.
     * @throws ServerErrorException if any errors have occurred.
     */
    private int openSSHConnection() throws ServerErrorException{
        // Setup for the ssh user and password
        ssh_user = configFile.getString("SSH_USER");
        ssh_pass = configFile.getString("SSH_PASSWORD");
        // IPs for remote host and the remote host with port forwarding.
        String remote_host_1 = configFile.getString("REMOTE_HOST_1");
        String remote_host_2 = configFile.getString("REMOTE_HOST_2");
        // Database port
        Integer postgresql_port = Integer.parseInt(configFile.getString("POSTGRESQL_PORT"));
        // SSH port
        Integer ssh_port = Integer.parseInt(configFile.getString("SSH_PORT"));
        int assigned_port;
        
        try {
            // Open SSH session
            session = new JSch().getSession(ssh_user, remote_host_1, ssh_port);
            session.setPassword(ssh_pass);
            // Doesn't check if host is already known
            session.setConfig("StrictHostKeyChecking", "no");
            // Initialize connection
            session.connect();
            // Set port forwarding to the postgresql machine
            assigned_port = session.setPortForwardingL(postgresql_port, remote_host_2, postgresql_port);
        } catch (JSchException ex) {
            throw new ServerErrorException();
        }
        // Returns the assigned port.
        return assigned_port;
    }
}