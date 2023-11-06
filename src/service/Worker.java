package service;

import exceptions.EmailExistsException;
import exceptions.LoginCredentialException;
import exceptions.ServerErrorException;
import interfaces.Signable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import message.ResponseRequest;

/**
 * This class represents a worker thread responsible managing the petitions of
 * the client such as sign-up or sign-in and sending back appropriate responses.
 *
 * @author Irati
 */
public class Worker extends Thread {

    private Socket client;
    private Signable signable;
    private ObjectOutputStream write;
    private ObjectInputStream read;
    private static final Logger LOGGER = Logger.getLogger(" package dataAcess");

    /**
     * Initializes a new worker thread with the client socket and signable
     * implementation.
     *
     * @param client The client's socket connection.
     * @param signable DAO implementation of the Signable interface.
     */
    public Worker(Socket client, Signable signable) {
        this.client = client;
        this.signable = signable;
    }

    /**
     * Runs the worker thread to process client requests and send responses.
     */
    @Override
    public void run() {
        ResponseRequest responseRequest = new ResponseRequest();
        try {
            //Instance of ObjectOutputStream and ObjectImputStream
            write = new ObjectOutputStream(client.getOutputStream());
            read = new ObjectInputStream(client.getInputStream());
            //Reads the responseRequest sent by the client
            responseRequest = (ResponseRequest) read.readObject();

            //Takes the recieved message to make a SignUp or a SignIn
            if (responseRequest.getMessage() == Message.SIGNUP) {
                responseRequest.setUser(signable.signUp(responseRequest.getUser()));
            } else {
                responseRequest.setUser(signable.signIn(responseRequest.getUser()));

            }
            //If there isn't an error, establishes the OK response
            responseRequest.setMessage(Message.RESPONSE_OK);

        } catch (ServerErrorException ex) {
            //If there is a ServerErrorException catches it and creates a ResponseRequest
            LOGGER.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            responseRequest = new ResponseRequest(null, Message.SERVER_ERROR);
        } catch (EmailExistsException ex) {
            //If there is a EmailExistsException catches it and creates a ResponseRequest
            LOGGER.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            responseRequest = new ResponseRequest(null, Message.EMAIL_EXITS_ERROR);
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            responseRequest = new ResponseRequest(null, Message.SERVER_ERROR);
        } catch (LoginCredentialException ex) {
            //If there is a LoginCredentialException catches it and creates a ResponseRequest
            LOGGER.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            responseRequest = new ResponseRequest(null, Message.CREDENTIAL_ERROR);
        } finally {
            try {
                //Writes the response
                write.writeObject(responseRequest);
                //Close ObjectOutputStream, ObjectImputStream and the socket
                read.close();
                write.close();
                client.close();
                //Calls the method to decrease the connections count
                Server.closeWorker();
            } catch (IOException ex) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
