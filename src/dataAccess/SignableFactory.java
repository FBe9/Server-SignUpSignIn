package dataAccess;

import interfaces.Signable;

/**
 * A factory class to create instance of Signable interface.
 * 
 *
 * @author Irati
 */
public class SignableFactory {
    public static Signable getSignable(){
        //Returns the DAO implementation
        return new DAO();
    }
}
