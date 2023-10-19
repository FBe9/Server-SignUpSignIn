package dataAccess;

import exceptions.DatabaseErrorException;
import exceptions.EmailExistsException;
import exceptions.LoginCredentialException;
import exceptions.ServerErrorException;
import interfaces.Signable;
import models.User;

/**
 *
 * @author alexs
 */
public class DAO implements Signable{

    @Override
    public User signUp(User user) throws ServerErrorException, EmailExistsException, DatabaseErrorException {
        return user;
    }

    @Override
    public User signIn(User user) throws ServerErrorException, LoginCredentialException, DatabaseErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
