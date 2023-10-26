package service;

import dataAccess.Pool;
import dataAccess.SignableFactory;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //Get the port form a property file for the socket.
            server = new ServerSocket(Integer.parseInt(ResourceBundle.getBundle("config.Config").getString("PORT")));
            logger.info("Waiting for the connection");

            //Calls the method waitClose that creates a thread that is in charge of clossing the server.
            waitClose();
            //A loop that listens the client petitions
            while (true) {
                Socket client = server.accept();
                //Calls the method to initialize worker threads.
                initializeWorker(client);
            }

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
        if (connections <= Integer.parseInt(ResourceBundle.getBundle("config.Config").getString("MAX_CONNECTIONS"))) {
            //Get a signable
            Signable signable = (Signable) SignableFactory.getSignable(pool);
            //Initialize the worker
            Worker worker = new Worker(client, signable);
            worker.start();
            //Increments the connections' counter
            connections++;

        } else {
            maxConnections(client);
        }

    }

    /**
     * Decreases the count of active connections.
     */
    public static synchronized void closeWorker() {
        //Decrease the connections' counter
        connections--;
    }

    /**
     * Initiates the thread in change of clossing the server.
     */
    public static void waitClose() {
        CloseThread close = new CloseThread();
        close.start();
    }

    public static void maxConnections(Socket client) {
        //Gets an ObjectOutputStream to write.
        ObjectOutputStream write = null;
        try {
            write = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Creates a responde for the client.
        ResponseRequest response = new ResponseRequest(null, Message.SERVER_CAPACITY_ERROR);
        try {
            //Sends the response to the client.
            write.writeObject(response);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
