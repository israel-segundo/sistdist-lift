package com.lift.client;

import com.lift.common.Operation;
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
    private static final int SUCCESS              = 0;
    
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
        
        Transaction transaction = new Transaction(Operation.FILES, null);
        Result result           = sendOperationToDaemon(transaction);
        
        // Do something with result...
        if(result.getReturnCode() == SUCCESS) {
            
            // TODO: sort the list by date added
            Map<String, RepositoryFile> map = (HashMap<String, RepositoryFile>)result.getResult();
            Set<Map.Entry<String, RepositoryFile>> fileSet = map.entrySet();
            // TODO: make the output fixed for the size of the longest entry
            StringBuilder sb = new StringBuilder();

            String format = "%-30s%-20s%-20s%-30s%-20s\n";
            sb.append( String.format(format, "LOCATION", "SIZE", "FILE ID", "DATE ADDED", "HITS") );

            fileSet.forEach((entry) -> {
                String location = entry.getValue().getName();
                String size = String.valueOf(entry.getValue().getSize());
                String fileID = entry.getValue().getGUID();
                String dateAdded = entry.getValue().getDateAdded();
                String hits = String.valueOf(entry.getValue().getHits());
                sb.append( String.format(format, location, size, fileID, dateAdded, hits) );
            });

            System.out.println(sb.toString());
            
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void add(String filePath) {
        
        Transaction transaction = new Transaction(Operation.ADD, filePath);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            // Do nothing
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void get(String UFL) {
    }
    
    public void id() {
        Transaction transaction = new Transaction(Operation.ID, null);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            System.out.format("%-10s%-35s\n", "GUID is:", result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void rm(String fileID) {
        Transaction transaction = new Transaction(Operation.RM, fileID);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            System.out.println(result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void share(String filePath) {
        Transaction transaction = new Transaction(Operation.SHARE, filePath);
        Result result           = sendOperationToDaemon(transaction);

        if (result.getReturnCode() == SUCCESS) {
            System.out.println(result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void ufl(String fileID) {
        Transaction transaction = new Transaction(Operation.UFL, fileID);
        Result result           = sendOperationToDaemon(transaction);

        if (result.getReturnCode() == SUCCESS) {
            System.out.printf("%-10s%-30s\n", "UFL is:", result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void version() {
        Transaction transaction = new Transaction(Operation.VERSION, null);
        Result result           = sendOperationToDaemon(transaction);
        String[] serverValues   = (String[])result.getResult();
        
        System.out.printf("Client:\n");
        String format = " %-15s%s\n";
        System.out.printf(format, "Version:", CLIENT_VERSION);
        System.out.printf(format, "Built:",   CLIENT_BUILD_DATE);
        System.out.printf("\nServer:\n");
        System.out.printf(" %-15s%s\n", "Version:", serverValues[0]);
        System.out.printf(" %-15s%s\n", "Built:",   serverValues[1]);
        System.out.printf("\n");
    }
}
