package service;

import com.jcraft.jsch.Session;
import dataAccess.Pool;
import static java.lang.System.exit;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This class represents a thread responsible of shutting down the server.
 *
 * @author Irati
 */
public class CloseThread extends Thread {

    private static final Logger logger = Logger.getLogger(CloseThread.class.getName());
    Scanner scanner = new Scanner(System.in);
    Session session;

    public CloseThread(Session session) {
        session = this.session;
    }

    /**
     * Runs the thread to check for the action to shut down the server.
     */
    @Override
    public void run() {
        //Logger to indicate which key must be pressed to close the server
        logger.info("Server is currently running. If you want to finish it PRESS 1");
        //Waits for the response
        int userInput = scanner.nextInt();
        //If the established key is pressed shuts down the server with an exit(0)
        if (userInput == 1) {
            logger.info("Server is shutting down");
            //Close the connections of the pool
            Pool pool = new Pool();
            pool.closeAllConnections();
            if (session != null) {
                //Close SSH session
                session.disconnect();
                logger.info("SSH disconnected.");
            }
            exit(0);
        }

    }
}
