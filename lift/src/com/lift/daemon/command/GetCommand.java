package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.common.Operation;
import com.lift.daemon.Daemon;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.Transaction;
import java.io.File;
import java.io.FileOutputStream;
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
        
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];
        
        // Connect to server here and get below info...
        String hostname     = "HARDCODED";
        int port            = 51223;
        
        Result<RepositoryFile> response = requestMetadataFromClient(hostname, port, fileID);
        
        if(response.getReturnCode() == Daemon.SUCCESS) {
            
            RepositoryFile fileMetadata = response.getResult();
            // Workaround to get the filename regarding of filesystem:
            String[] originalFileLocation = fileMetadata.getName().replaceAll("/", "#").replaceAll("\\", "#").split("#");
            // This might cause NPE issues:
            String fileName = originalFileLocation[originalFileLocation.length - 1];
            
            result = requestFileFromClient(hostname, port, fileID, fileName);
            
            
        } else {
            result.setReturnCode(1);
            result.setMessage("Daemon: Could not retrieve the file metadata. File might not be available.");
        }
        
        return result;
    }
    
    // TODO: Can we refactor this method with the one in ClientManager?
    public Result requestMetadataFromClient(String hostname, int port, String fileID) {
        Result result = new Result();
        
        try (Socket sock = new Socket(hostname, port);
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                Transaction transaction = new Transaction(Operation.META, fileID);
                System.out.println("[ INFO ] Trying to establish a connection to the client: " + hostname + ":" + port);

                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                System.out.println("[ INFO ] Received result from client: " + result);
                

        } catch (UnknownHostException e) {
                System.err.println("[ ERROR ] Don't know about host " + hostname);
        } catch (IOException e) {
                System.err.println("[ ERROR ] Can not connect to Lift client at [" + hostname + ":" + port + "].");
        } catch (ClassNotFoundException e) {
                System.err.println("[ ERROR ] ClassNotFoundException found!");
        }        
        
        return result;
    }
    
    // TODO: Can we refactor this method with the one in ClientManager?
    public Result requestFileFromClient(String hostname, int port, String fileID, String fileName) {
        Result result       = new Result();
        boolean isFileSaved = false;
        
        try (Socket sock = new Socket(hostname, port);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream());) {
            
            Transaction transaction = new Transaction(Operation.RETRIEVE, fileID);
            System.out.println("[ INFO ] Trying to establish a connection to the client: " + hostname + ":" + port);

            // Send
            out.writeObject(transaction);
            
            // Wait and Receive Result
            result = (Result) in.readObject();
            
            if(result.getReturnCode() != Daemon.SUCCESS) {
                return result;
            }
            
            byte[] data  = (byte[]) result.getResult();
            
            System.out.println("[ INFO ] Received from client [" + hostname + "] " + data.length + " bytes.");
            isFileSaved = writeFile(data, fileName);
            
            if (isFileSaved) {
                result.setReturnCode(0);
                result.setResult(Daemon.SHARED_DIR_ROUTE.getAbsolutePath() + File.pathSeparator + fileName);
            } else {
                result.setReturnCode(1);
                result.setMessage("Daemon: File " + Daemon.SHARED_DIR_ROUTE.getAbsolutePath() + File.pathSeparator + fileName + " could not be saved.");
            }
            
        } catch (IOException e) {
            System.out.println("[ ERROR ] Can not connect to Lift client at [" + hostname + ":" + port + "]");
            result.setReturnCode(2);
            result.setMessage("Daemon: Can not connect to Lift client at [" + hostname + ":" + port + "]. Client might not be available.");
        } catch (ClassNotFoundException e) {
            System.out.println("[ ERROR ] ClassNotFoundException found!");
        }            
        
        return result;
    }
    
    public static boolean writeFile(byte[] data, String fileName) {
        boolean isFileSaved  = false;
        FileOutputStream fos = null;
        File writeLocation   = new File(Daemon.SHARED_DIR_ROUTE.getAbsolutePath() + File.pathSeparator + fileName);
        try {
            fos = new FileOutputStream(writeLocation);
            fos.write(data);
            fos.close();
            isFileSaved = true;
        } catch (IOException e) {
            isFileSaved = false;
        }
        
        return isFileSaved;
    }
}
