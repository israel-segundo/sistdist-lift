package com.lift.daemon.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
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
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;


public class GetCommand {
    
    private static final Logger logger  = new Logger(GetCommand.class);
    
    private String ufl                = null;
    private Socket localSock          = null;
    private String metadataJson       = null;
    private boolean isReadyToTransfer = false;
    private long totalBytes           = 0;

    public GetCommand(String ufl, String metadataJson, Socket sock) {
        this.ufl  = ufl;
        this.localSock = sock;
        this.metadataJson = metadataJson;
    }
    
    public Result execute() {
        Result result       = new Result();
        
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];
        
        // Connect to server here and get below info...
        String hostname     = "localhost";
        int port            = 45120;
        
        //Result<RepositoryFile> metadataResponse = executeMetaInRemoteClient(hostname, port, fileID);
        
        try (ObjectOutputStream out = new ObjectOutputStream(localSock.getOutputStream());) 
        {                
            Gson gson = new Gson();
            Type type = new TypeToken<RepositoryFile>(){}.getType(); 
            RepositoryFile repositoryFile = gson.fromJson(metadataJson, type);

            File f = new File(repositoryFile.getName());
            String fileName = f.getName();

            totalBytes = repositoryFile.getSize();

            result = executeRetrieveInRemoteClient(hostname, port, fileID, fileName, localSock);
                
        } catch (IOException ex) {
            
            logger.error("Error when connecting to: " + localSock.getLocalSocketAddress());
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
            Transaction transaction = new Transaction(Operation.META, new String[]{fileID});
            logger.info("Trying to establish a connection to the client: " + hostname + ":" + port);

            // Send
            out.writeObject(transaction);

            // Wait and Receive Result
            result = (Result) in.readObject();

            if (result.getReturnCode() == Daemon.SUCCESS) {
                isReadyToTransfer = true;
            }
           
            logger.info("Received result from client: " + result);
                

        } catch (UnknownHostException e) {
                logger.error("Don't know about host " + hostname);
        } catch (IOException e) {
                logger.error("Can not connect to Lift client at [" + hostname + ":" + port + "].");
        } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException found!");
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
            
            Transaction transaction = new Transaction(Operation.RETRIEVE, new String []{fileID});
            logger.info("Trying to establish a connection to the client: " + hostname + ":" + port);

            // Send the RETRIEVE operation to notify remote client to send bytes of data
            remoteOut.writeObject(transaction);
            
            
            // Read actual bytes from remote client
            byte[] buffer = new byte[1024_000]; // 100 kb
            int length;            
            File writeLocation = new File(Daemon.sharedDirFile.getAbsolutePath() + File.pathSeparator + fileName);
            FileOutputStream fos = new FileOutputStream(writeLocation);
            
            
            while (current != totalBytes && (length = remoteIn.read(buffer)) > 0){
                // report to client launcher the bytes read for progress bar
                current = current + length;
                
                if( null != localSock && localSock.isConnected()){
                    localOut.writeLong(current);
                }
                
                logger.info("Received from client [" + hostname + "] " + length + " bytes.");
                
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
                result.setResult(Daemon.sharedDirFile.getAbsolutePath() + File.pathSeparator + fileName);
            } else {
                result.setReturnCode(1);
                result.setMessage("Daemon: File " + Daemon.sharedDirFile.getAbsolutePath() + File.pathSeparator + fileName + " could not be saved.");
            }
            
        } catch (IOException e) {
            logger.error("Can not connect to Lift client at [" + hostname + ":" + port + "]");
            result.setReturnCode(2);
            result.setMessage("Daemon: Can not connect to Lift client at [" + hostname + ":" + port + "]. Client might not be available.");
        } 
        
        return result;
    }
    
}
