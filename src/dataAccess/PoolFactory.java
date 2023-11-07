package dataAccess;

/**
 * A factory class to create instances of classes that implement the
 * PoolCreatable interface.
 *
 * @author Irati
 */
public class PoolFactory {

    private static Pool pool;

    /**
     * Retrieves a singleton instance of a class that implements the
     * PoolCreatable interface.
     *
     * @return A singleton instance of the class that implements the
     * PoolCreatable interface.
     */
    public static synchronized PoolCreatable getPool() {
        if (pool == null) {
            pool = new Pool();
        }
        return pool;
    }
}
