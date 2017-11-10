package com.lift.client;

import com.lift.daemon.Daemon;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.Transaction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * This class handles the client operations received by the Client Launcher.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientManager {
    
    private final String repoFileRoute    = "repo.json";
    private final String sharedDirRoute   = "/scratch/lift/";
    private final int daemonPort          = 45115;
    private final String hostName         = "localhost";
    
    // TODO: replace this with IPC for daemon-clientManager
    private Daemon daemon = new Daemon();
    
    private Result sendOperationToDaemon(Transaction transaction){
        
        Result result = null;

        System.out.println("[ INFO ] Trying to establish a connection to the daemon ");

        try (Socket sock = new Socket(hostName, daemonPort);
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                System.out.println("[ INFO ] Received result from daemon: " + result);
                

        } catch (UnknownHostException e) {
                System.err.println("[ ERROR ] Don't know about host " + hostName);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("[ ERROR ] Couldn't get I/O for the connection to " + hostName);
                System.exit(1);
        } catch (ClassNotFoundException e) {
                System.err.println("[ ERROR ] ClassNotFoundException found!");
                System.exit(1);
        }        
        
        return result;
    }
    
    public void files() {
        //daemon.files();
        
        Transaction transaction = new Transaction("files", null);
        Result result           = sendOperationToDaemon(transaction);
        
        
        // Do something with result...
        
        System.out.println(result.getResult());
    }
    
    public void add(String filePath) {
        
        Transaction transaction = new Transaction("add", filePath);
        Result result           = sendOperationToDaemon(transaction);        
    }
    

    
    public void get(String UFL) {
    }
    
    public void id() {
        daemon.id();
    }
    
    public void rm(String fileID) {
        daemon.rm(fileID);
    }
    
    public void share(String filePath) {
    }
    
    public void ufl(String fileID) {
        daemon.ufl(fileID);
    }
    
    public void version() {
        daemon.version();
    }
}
