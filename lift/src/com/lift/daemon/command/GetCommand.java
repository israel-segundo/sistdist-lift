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
import java.util.logging.Level;
import java.util.logging.Logger;


public class GetCommand implements LiftCommand {
    private String ufl                = null;
    private Socket localSock          = null;
    private boolean isReadyToTransfer = false;
    private long totalBytes           = 0;

    public GetCommand(String ufl, Socket sock) {
        this.ufl  = ufl;
        this.localSock = sock;
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
        
        Result<RepositoryFile> metadataResponse = executeMetaInRemoteClient(hostname, port, fileID);
        
        try (ObjectOutputStream out = new ObjectOutputStream(localSock.getOutputStream());) 
        {
            if(isReadyToTransfer) {
                // Send back to client the metadata
                out.writeObject(metadataResponse);
             
                RepositoryFile fileMetadata = metadataResponse.getResult();
                totalBytes = fileMetadata.getSize();
                // Workaround to get the filename regarding of filesystem:
                String[] originalFileLocation = fileMetadata.getName().replaceAll("/", "#").replaceAll("\\", "#").split("#");
                // This might cause NPE issues:
                String fileName = originalFileLocation[originalFileLocation.length - 1];

                result = executeRetrieveInRemoteClient(hostname, port, fileID, fileName, localSock);
                
            } else {
                result.setReturnCode(1);
                result.setMessage(metadataResponse.getMessage());
                
                return result;
            }
        } catch (IOException ex) {
            Logger.getLogger(GetCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return result;
    }
    
    // TODO: Can we refactor this method with the one in ClientManager?
    public Result executeMetaInRemoteClient(String hostname, int port, String fileID) {
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

            if (result.getReturnCode() == Daemon.SUCCESS) {
                isReadyToTransfer = true;
            }
           
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
    public Result executeRetrieveInRemoteClient(String hostname, int port, String fileID, String fileName, Socket localSock) {
        Result result       = new Result();
        boolean isFileSaved = false;
        long current        = 0;
        
        try (Socket remoteSock            = new Socket(hostname, port);
             ObjectOutputStream localOut  = new ObjectOutputStream(localSock.getOutputStream());
             ObjectOutputStream remoteOut = new ObjectOutputStream(remoteSock.getOutputStream());
             ObjectInputStream remoteIn   = new ObjectInputStream(remoteSock.getInputStream());
        ) {
            
            Transaction transaction = new Transaction(Operation.RETRIEVE, fileID);
            System.out.println("[ INFO ] Trying to establish a connection to the client: " + hostname + ":" + port);

            // Send the RETRIEVE operation to notify remote client to send bytes of data
            remoteOut.writeObject(transaction);
            
            
            // Read actual bytes from remote client
            byte[] buffer = new byte[1024_000]; // 100 kb
            int length;            
            File writeLocation = new File(Daemon.SHARED_DIR_ROUTE.getAbsolutePath() + File.pathSeparator + fileName);
            FileOutputStream fos = new FileOutputStream(writeLocation);
            
            
            while (current != totalBytes && (length = remoteIn.read(buffer)) > 0){
                // report to client launcher the bytes read for progress bar
                current = current + length;
                localOut.writeLong(current);
                
                System.out.println("[ INFO ] Received from client [" + hostname + "] " + length + " bytes.");
                
    	    	// write buffer to file here...
                try {
                    fos.write(buffer, 0, length);
                    isFileSaved = true;
                } catch (IOException e) {
                    isFileSaved = false;
                    break;
                }
                
    	    }
            
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
        } 
        
        return result;
    }
    
}
