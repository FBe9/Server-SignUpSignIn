package service;

import dataAccess.Pool;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import dataAccess.SignableFactory;
import exceptions.ServerErrorException;
import exceptions.ServerMaxCapacityException;
import interfaces.Signable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import message.ResponseRequest;

/**
 * The Server class represents a server application that connects with the
 * client side. It listens for client connections and initializes worker threads
 * to manage the requets. When the server reaches its maximum capacity, it sends
 * a server capacity error response to clients.
 *
 * @author Irati
 */
public class Server {

    private static ServerSocket server = null;
    private static int connections = 0;
    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private static Pool pool = new Pool();
    private static Session session;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //Get the port form a property file for the socket.
            server = new ServerSocket(Integer.parseInt(ResourceBundle.getBundle("config.Config").getString("PORT")));
            logger.info("Waiting for the connection");

            // Open SSH connection
            openSSHConnection();

            //Calls the method waitClose that creates a thread that is in charge of clossing the server.
            waitClose();
            //A loop that listens the client petitions
            while (true) {
                Socket client = server.accept();
                //Calls the method to initialize worker threads.
                initializeWorker(client);
            }
        } catch (ServerErrorException ex) {
            logger.info("SERVER ERROR. Couldn't connect to DB. Check that everything is on and IP is correct.");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes a worker that manage a client connection. If the server can
     * handle the petition, a worker thread is created to serve the client. If
     * the server is at its maximun capacity, it sends a server capacity error
     * response to the client.
     *
     * @param client The client's socket connection.
     */
    private static void initializeWorker(Socket client) {
        //Gets from a property file the maximun connections
        if (connections < Integer.parseInt(ResourceBundle.getBundle("config.Config").getString("MAX_CONNECTIONS"))) {
            //Get a signable
            Signable signable = (Signable) SignableFactory.getSignable(pool);
            //Initialize the worker
            Worker worker = new Worker(client, signable);
            worker.start();
            //Increments the connections' counter
            connections++;

        } else {
            //If the maximun capacity has been reached. Seeds a exception
            try {
                throw new ServerMaxCapacityException();
            } catch (ServerMaxCapacityException ex) {
                try {
                    //Gets an ObjectOutputStream to write.
                    ObjectOutputStream write = new ObjectOutputStream(client.getOutputStream());
                    //Creates a responde for the client.
                    System.out.println("SERVER MAX CAPACITY");
                    ResponseRequest response = new ResponseRequest(null, Message.SERVER_CAPACITY_ERROR);
                    //Sends the response to the client.
                    write.writeObject(response);
                } catch (IOException ex1) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    /**
     * Decreases the count of active connections.
     */

    public synchronized static void closeWorker() {
        //Decrease the connections' counter
        connections--;
    }

    /**
     * Initiates the thread in change of clossing the server.
     */
    public static void waitClose() {
        CloseThread close = new CloseThread(session);
        close.start();
    }

    /**
     * Creates an SSH connection to the postgres DB.
     *
     * @return the assigned port during the port forwarding.
     * @throws ServerErrorException if any errors have occurred.
     */
    private static void openSSHConnection() throws ServerErrorException {
        ResourceBundle configFile = ResourceBundle.getBundle("config.Config");

        // Setup for the ssh user and password
        String ssh_user = configFile.getString("SSH_USER");
        String ssh_pass = configFile.getString("SSH_PASSWORD");
        // IPs for remote host and the remote host with port forwarding.
        String remote_host_1 = configFile.getString("REMOTE_HOST_1");
        String remote_host_2 = configFile.getString("REMOTE_HOST_2");
        // Database port
        Integer postgresql_port = Integer.parseInt(configFile.getString("POSTGRESQL_PORT"));
        // SSH port
        Integer ssh_port = Integer.parseInt(configFile.getString("SSH_PORT"));

        try {
            // Open SSH session
            session = new JSch().getSession(ssh_user, remote_host_1, ssh_port);
            session.setPassword(ssh_pass);
            // Doesn't check if host is already known
            session.setConfig("StrictHostKeyChecking", "no");
            // Initialize connection
            session.connect();
            // Set port forwarding to the postgresql machine
            session.setPortForwardingL(postgresql_port, remote_host_2, postgresql_port);
            logger.info("SSH connection successful.");
        } catch (JSchException ex) {
            throw new ServerErrorException();
        }
    }
}
