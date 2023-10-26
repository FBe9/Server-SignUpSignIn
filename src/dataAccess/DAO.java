package dataAccess;

import com.jcraft.jsch.*;
import exceptions.DatabaseErrorException;
import exceptions.EmailExistsException;
import exceptions.LoginCredentialException;
import exceptions.ServerErrorException;
import interfaces.Signable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import models.Privilege;
import models.User;

/**
 * Class that implements the SignUp and SignIn methods of the Signable
 * interface.
 *
 * @author Olivia
 */
public class DAO implements Signable {

    private Connection con;
    private PreparedStatement stmt;
    private Pool connection;
    private ResultSet rs;

    public DAO(Pool pool) {
        this.connection = pool;
    }

    /**
     * Connects to DB via a connection taken from the Pool and inserts all the
     * user parameters in the necessary DB tables.
     *
     * @param user is the received user.
     * @return the user if execution is successful.
     * @throws ServerErrorException if the connection to the DB failed.
     * @throws EmailExistsException if the email already exists in the DB.
     * @throws DatabaseErrorException for all other DB failures.
     */
    @Override
    public User signUp(User user) throws ServerErrorException, EmailExistsException, DatabaseErrorException {
        final String SELECTEMAIL = "SELECT * FROM public.res_users WHERE login = ?";
        final String INSERTPARTNER = "INSERT INTO public.res_partner(company_id, name, street, zip, city, email, active, create_date) VALUES('1',  ?,  ?,  ?,  ?,  ?, true, now())";
        final String SELECTPARTNER = "SELECT id, create_date FROM public.res_partner WHERE id IN (SELECT MAX(id) FROM public.res_partner);";
        final String INSERTUSER = "INSERT INTO public.res_users(company_id, partner_id, active, login, password, notification_type, create_date) VALUES('1', ?, True, ?, ?, 'email', ?)";
        final String SELECTUSER = "SELECT MAX(id) FROM public.res_users";
        final String INSERTGROUP_USER_REL = "INSERT INTO public.res_groups_users_rel(gid, uid) VALUES ('1', ?),('7', ?),('8', ?),('9', ?)";
        final String INSERTGROUP_USER_REL_ADMIN = "INSERT INTO public.res_groups_users_rel(gid, uid) VALUES ('1', ?),('2', ?),('3', ?),('4', ?),('7', ?),('8', ?),('9', ?)";
        final String INSERTCOMPANY_USER_REL = "INSERT INTO public.res_company_users_rel(cid, user_id)VALUES ('1', ?);";

        Integer partner_id = null;
        Integer uid = null;
        Timestamp create_date = null;

        try {
            // Open the connection to DB
            try {
                con = connection.takeConnection();
            } catch (ServerErrorException ex) {
                throw new ServerErrorException();
            }

            if (con != null) {
                // Establish statement to select posibly existing email from DB.
                stmt = con.prepareStatement(SELECTEMAIL);
                stmt.setString(1, user.getEmail());
                rs = stmt.executeQuery();

                while (rs.next()) {
                    throw new EmailExistsException();
                }

                // Establish the statatenent to insert into res_partner
                stmt = con.prepareStatement(INSERTPARTNER);
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getStreet());
                stmt.setString(3, user.getZip());
                stmt.setString(4, user.getCity());
                stmt.setString(5, user.getEmail());
                if (stmt.executeUpdate() == 0) {
                    throw new DatabaseErrorException();
                }

                // Second statement to select id and creation date from res_partner
                stmt = con.prepareStatement(SELECTPARTNER);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    partner_id = rs.getInt(1);
                    create_date = rs.getTimestamp(2);
                }

                // Prepare third statement to insert the new user into res_users
                stmt = con.prepareStatement(INSERTUSER);
                stmt.setInt(1, partner_id);
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());
                stmt.setTimestamp(4, create_date);
                // Execute statement
                if (stmt.executeUpdate() == 0) {
                    throw new DatabaseErrorException();
                }

                // Fourth statement to select UID from res_users
                stmt = con.prepareStatement(SELECTUSER);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    uid = rs.getInt(1);
                }

                // Prepare fifth statement to insert user into res_groups_users_rel
                if (user.getPrivilege() == Privilege.ADMIN) {
                    // ONLY IF THE USER TYPE IS ADMIN
                    stmt = con.prepareStatement(INSERTGROUP_USER_REL_ADMIN);
                    stmt.setInt(5, uid);
                    stmt.setInt(6, uid);
                    stmt.setInt(7, uid);
                } else {
                    // ONLY IF THE USER TYPE IS USER
                    stmt = con.prepareStatement(INSERTGROUP_USER_REL);
                }
                stmt.setInt(1, uid);
                stmt.setInt(2, uid);
                stmt.setInt(3, uid);
                stmt.setInt(4, uid);
                // Execute statement
                if (stmt.executeUpdate() == 0) {
                    throw new DatabaseErrorException();
                }

                // Prepare sixth statement to insert user into res_company_users_rel
                stmt = con.prepareStatement(INSERTCOMPANY_USER_REL);
                stmt.setInt(1, uid);
                //Execute statement
                if (stmt.executeUpdate() == 0) {
                    throw new DatabaseErrorException();
                }

                //Close the connection
                if (stmt != null) {
                    stmt.close();
                }
            }
            connection.returnConnection(con);
        } catch (SQLException ex) {
            throw new DatabaseErrorException();
        }
        return user;
    }

    @Override
    public User signIn(User user) throws ServerErrorException, LoginCredentialException, DatabaseErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

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
