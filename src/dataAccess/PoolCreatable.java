package dataAccess;

import exceptions.ServerErrorException;
import java.sql.Connection;

/*This interface defines the methods for creating a Pool.
 * @author Irati
 */
public interface PoolCreatable {

    /**
     * Retrieves a database connection.
     *
     * @return connection object representing a database connection.
     * @throws ServerErrorException If an error occurs while trying to obtain a
     * connection.
     */
    public Connection takeConnection() throws ServerErrorException;

    /*
     * Returns a previously obtained database connection to the connection pool for reuse.
     */
    public void returnConnection(Connection con);

    /**
     * Closes all connections managed by pool.
     */
    public void closeAllConnections();
}
