package service;

import static java.lang.System.exit;
import java.util.Scanner;

/**
 * This class represents a thread responsible of shutting down the server.
 * 
 * @author Irati
 */
public class CloseThread extends Thread {

    Scanner scanner = new Scanner(System.in);
      /**
     * Runs the thread to check for the action to shut down the server.
     */
    @Override
    public void run() {
        //Syso to indicate which key must be pressed to close the server
        System.out.println("Server is currently running. If you want to finish it PRESS 1");
        //Waits for the response
        int userInput = scanner.nextInt();
        //If the established key is pressed shuts down the server with an exit(0)
        if (userInput == 1) {
            System.out.println("Server is shutting down");
            //INCLUIR CERRAR TODAS LAS CONEXIONES DEL POOL
            exit(0);
        }

    }
}
