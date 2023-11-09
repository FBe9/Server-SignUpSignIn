package dataAccess;

import interfaces.Signable;

/**
 * A factory class to create instances of classes that implement the Signable
 * interface.
 *
 * @author Irati
 */
public class SignableFactory {

    /**
     * Default empty constructor.
     */
    public SignableFactory() {
    }

    /**
     * Returns an instance of a class that implements the Signable interface.
     *
     * @return a Signable object with the DAO implementation.
     */
    public static Signable getSignable() {
        return new DAO();
    }
}
