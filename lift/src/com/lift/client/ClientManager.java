package com.lift.client;

import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.Transaction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class handles the client operations received by the Client Launcher.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientManager {
    
    private static final String CLIENT_VERSION    = "1.0.0";
    private static final String CLIENT_BUILD_DATE = Calendar.getInstance().getTime().toString();
    private static final int SUCCESS = 0;
    
    private static final String SHARED_DIR_ROUTE  = "/scratch/lift/";
    private static final int DAEMON_PORT          = 45115;
    private static final String DAEMON_HOSTNAME   = "localhost";
    
    
    private Result sendOperationToDaemon(Transaction transaction){
        
        Result result = null;

        System.out.println("[ INFO ] Trying to establish a connection to the daemon ");

        try (Socket sock = new Socket(DAEMON_HOSTNAME, DAEMON_PORT);
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                System.out.println("[ INFO ] Received result from daemon: " + result);
                

        } catch (UnknownHostException e) {
                System.err.println("[ ERROR ] Don't know about host " + DAEMON_HOSTNAME);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("Can not connect to Lift daemon at [" + DAEMON_HOSTNAME + "]. Is the Lift daemon running?");
                System.exit(1);
        } catch (ClassNotFoundException e) {
                System.err.println("[ ERROR ] ClassNotFoundException found!");
                System.exit(1);
        }        
        
        return result;
    }
    
    public void files() {
        
        Transaction transaction = new Transaction("files", null);
        Result result           = sendOperationToDaemon(transaction);
        
        // Do something with result...
        if(result.getReturnCode() == SUCCESS) {
            
            // TODO: sort the list by date added
            Map<String, RepositoryFile> map = (HashMap<String, RepositoryFile>)result.getResult();
            Set<Map.Entry<String, RepositoryFile>> fileSet = map.entrySet();
            // TODO: make the output fixed for the size of the longest entry
            StringBuilder sb = new StringBuilder();

            String format = "%-30s%-20s%-20s%-30s%-20s\n";
            sb.append( String.format(format, "LOCATION", "SIZE", "FILE ID", "DATE ADDED", "HITS"));

            fileSet.forEach((entry) -> {
                String location = entry.getValue().getName();
                String size = entry.getValue().getSize() + "";
                String fileID = entry.getValue().getGUID();
                String dateAdded = entry.getValue().getDateAdded();
                String hits = entry.getValue().getHits() + "";
                sb.append( String.format(format, location, size, fileID, dateAdded, hits));
            });

            System.out.println(sb.toString());
            
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void add(String filePath) {
        
        Transaction transaction = new Transaction("add", filePath);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            System.out.println("File added. ID: " + result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    

    
    public void get(String UFL) {
    }
    
    public void id() {
    }
    
    public void rm(String fileID) {
    }
    
    public void share(String filePath) {
    }
    
    public void ufl(String fileID) {
    }
    
    public void version() {
        System.out.printf("Client:\n");
        System.out.printf(" %-15s%s\n", "Version:", CLIENT_VERSION);
        System.out.printf(" %-15s%s\n", "Built:",   CLIENT_BUILD_DATE);
    }
}
