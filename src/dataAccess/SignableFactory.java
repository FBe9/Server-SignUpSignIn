package dataAccess;

import interfaces.Signable;

/**
 * A factory class to create instance of Signable interface.
 * 
 *
 * @author Irati
 */
public class SignableFactory {
   
    public static synchronized Signable getSignable(Pool pool) {
        return new DAO(pool);
    }
}
