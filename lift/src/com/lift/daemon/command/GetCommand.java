package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.common.Operation;
import com.lift.daemon.Daemon;
import com.lift.daemon.Result;
import com.lift.daemon.Transaction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class GetCommand implements LiftCommand {
    private String ufl = null;

    public GetCommand(String ufl) {
        this.ufl = ufl;
    }
    
    @Override
    public Result execute() {
        Result result       = new Result();
        
        // Decode the UFL
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];
        
        // Connect to server and get infor for clientGUID associated data (ip, port)
        String hostname     = "HARDCODED";
        int port            = 51223;
        
        // Request Meta information
        Result metadata = requestMetadataFromClient(hostname, port, fileID);
        
        if(metadata.getReturnCode() == Daemon.SUCCESS) {
            // Request the file
            Result fileResult = requestFileFromClient(hostname, port, fileID);
            
            
        } else {
            result.setReturnCode(1);
            result.setMessage("Daemon: Could not retrieve the file. File might not be available.");
        }
        
        return result;
    }
    
    // TODO: Can we refactor this method with the one in ClientManager?
    public Result requestMetadataFromClient(String hostname, int port, String fileID) {
        Result result = null;
        
        Transaction transaction = new Transaction(Operation.META, fileID);

        System.out.println("[ INFO ] Trying to establish a connection to the client: " + hostname + ":" + port);

        try (Socket sock = new Socket(hostname, port);
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                System.out.println("[ INFO ] Received result from client: " + result);
                

        } catch (UnknownHostException e) {
                System.err.println("[ ERROR ] Don't know about host " + hostname);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("Can not connect to Lift client at [" + hostname + ":" + port + "]. The client might not be available.");
                System.exit(1);
        } catch (ClassNotFoundException e) {
                System.err.println("[ ERROR ] ClassNotFoundException found!");
                System.exit(1);
        }        
        
        return result;
    }
    
    // TODO: Can we refactor this method with the one in ClientManager?
    public Result requestFileFromClient(String hostname, int port, String fileID) {
        Result result = null;
        
        Transaction transaction = new Transaction(Operation.RETRIEVE, fileID);

        System.out.println("[ INFO ] Trying to establish a connection to the client: " + hostname + ":" + port);

        try (Socket sock = new Socket(hostname, port);
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                System.out.println("[ INFO ] Received result from client: " + result);
                

        } catch (UnknownHostException e) {
                System.err.println("[ ERROR ] Don't know about host " + hostname);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("Can not connect to Lift client at [" + hostname + ":" + port + "]. The client might not be available.");
                System.exit(1);
        } catch (ClassNotFoundException e) {
                System.err.println("[ ERROR ] ClassNotFoundException found!");
                System.exit(1);
        }        
        
        return result;
    }
}
