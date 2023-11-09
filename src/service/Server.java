package service;

import dataAccess.SignableFactory;
import exceptions.ServerErrorException;
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
 * client side. It listens for client connections and initialises worker threads
 * to manage the request. When the server reaches its maximum capacity, it sends
 * a server capacity error response to clients.
 *
 * @author Irati
 */
public class Server {

    private static ServerSocket server = null;
    private static int connections = 0;
    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    /**
     * Default empty constructor.
     */
    public Server() {
    }

    /**
     * Main method for the Server.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Get the port form a property file for the socket.
            server = new ServerSocket(Integer.parseInt(ResourceBundle.getBundle("config.config").getString("PORT")));
            logger.info("Waiting for the connection.");

            // Calls the method waitClose that creates a thread that is in charge of clossing the server.
            waitClose();
            // A loop that listens the client petitions
            while (true) {
                Socket client = server.accept();
                // Calls the method to initialize worker threads.
                initializeWorker(client);
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialises a worker that manage a client connection. If the server can
     * handle the petition, a worker thread is created to serve the client. If
     * the server is at its maximum capacity, it sends a server capacity error
     * response to the client.
     *
     * @param client The client's socket connection.
     */
    private static void initializeWorker(Socket client) {
        logger.info("Initialising the worker thread.");
        // Gets from a property file the maximun connections
        if (connections < Integer.parseInt(ResourceBundle.getBundle("config.config").getString("MAX_CONNECTIONS"))) {
            logger.info("There is a connection avaliable.");
            // Initialize the worker
            Worker worker = new Worker(client);
            worker.start();
            // Increments the connections' counter
            connections++;

        } else {
            logger.info("No connections avaliable.");
            // If the maximun capacity has been reached. Seeds a exception
            try {
                throw new ServerErrorException();
            } catch (ServerErrorException ex) {
                try {
                    // Gets an ObjectOutputStream to write.
                    ObjectOutputStream write = new ObjectOutputStream(client.getOutputStream());
                    // Creates a responde for the client.
                    ResponseRequest response = new ResponseRequest(null, Message.SERVER_CAPACITY_ERROR);
                    // Sends the response to the client.
                    write.writeObject(response);
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
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
        logger.info("Closing the connection.");
        // Decrease the connections' counter
        connections--;
    }

    /**
     * Initiates the thread in change of closing the server.
     */
    public static void waitClose() {
        logger.info("Initializing the thread that waits for closing the server");
        CloseThread close = new CloseThread();
        close.start();
    }
}
