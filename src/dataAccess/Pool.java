package dataAccess;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
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
    //private String url;
    private String user;
    private String pass;
    Stack<Connection> connections = new Stack<>();
    private static final Logger logger = Logger.getLogger(Pool.class.getName());
    Session session = null;

    /**
     * Constructs a new connection pool and initializes it.
     */
    public Pool() {

        configFile = ResourceBundle.getBundle("config.Config");
        user = configFile.getString("USERNAME");
        pass = configFile.getString("PASSWORD");

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
        // For SSH session(s)
        int assigned_port;
        String remote_host_1 = configFile.getString("REMOTE_HOST_1");
        String remote_host_2 = configFile.getString("REMOTE_HOST_2");
        Integer postgresql_port = Integer.parseInt(configFile.getString("POSTGRESQL_PORT"));
        Integer ssh_port = Integer.parseInt(configFile.getString("SSH_PORT"));

        //Checks if the stack is empty
        if (connections.isEmpty()) {
            try {
                // Open SSH session
                session = new JSch().getSession(user, remote_host_1, ssh_port);
                session.setPassword(pass);
                localUserInfo lui = new localUserInfo();
                session.setUserInfo(lui);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setConfig("Compression", "yes");
                session.connect();
                assigned_port = session.setPortForwardingL(postgresql_port, remote_host_2, postgresql_port);
                // URL with assigned SSH port
                StringBuilder url = new StringBuilder(configFile.getString("URL"));
                url.append(assigned_port).append(configFile.getString("DB"));

                //Creates a new connection
                con = DriverManager.getConnection(url.toString(), user, pass);
            } catch (SQLException ex) {
                throw new ServerErrorException();
            } catch (JSchException ex) {
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

    // User info for SSH sessions
    class localUserInfo implements UserInfo {

        String passwd;

        public String getPassword() {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            return true;
        }

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public void showMessage(String message) {
        }
    }
}
