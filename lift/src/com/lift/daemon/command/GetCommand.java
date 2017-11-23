package com.lift.daemon.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lift.common.AppConfig;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
import com.lift.common.Operation;
import com.lift.common.ServerConsumer;
import com.lift.daemon.Daemon;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.Transaction;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * This class handles the Lift GET operation:
 * 
 * $ lift get <ufl>
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class GetCommand implements Runnable {
    
    private static final Logger logger  = new Logger(GetCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
    private String ufl                = null;
    private String metadataJson       = null;
    private volatile Daemon.Sem sem   = null;
    private long totalBytes           = 0;

    private String serverHostIpAddress    = null;
    private int serverHostPort            = 0;
    
    public GetCommand(String ufl, Daemon.Sem s) {
        this.ufl  = ufl;
        this.sem = s;
    }
    
    public void setMetadata(String metadataJson) {
        this.metadataJson = metadataJson;
    }  
    
    public void getRemoteClientConnectionDetails(String clientGUID){
    
        ServerConsumer serverConsumer = null;
        
        // Connect to server
        try{
            serverConsumer = new ServerConsumer();
            
        }catch(Exception ex){
            logger.error("Cannot connect to server");
        }
        
        // Get connection details
        Map<String,String> connectionDetailsMap = serverConsumer.getConnectionDetails(clientGUID);
        
        if(connectionDetailsMap.isEmpty()){
            logger.error("There is no data of clientGUID in server");
            // TODO: handle error
        }
        
        this.serverHostIpAddress = connectionDetailsMap.get("ip");
        this.serverHostPort      = Integer.parseInt(connectionDetailsMap.get("port"));
    }
    
    
    public Result getMetadataFromRemoteClient(String ufl) {
        Result result = new Result();
        
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];
        
        // Get client details from server
        getRemoteClientConnectionDetails(clientGUID);
        
        
        try (Socket sock            = new Socket(this.serverHostIpAddress, this.serverHostPort);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in   = new ObjectInputStream(sock.getInputStream());
        ) {
            Transaction transaction = new Transaction(Operation.META, new String[]{fileID});
            logger.info("Trying to establish a connection to the client: " + this.serverHostIpAddress + ":" + this.serverHostPort);

            // Send
            out.writeObject(transaction);

            // Wait and Receive Result
            result = (Result) in.readObject();

            if (result.getReturnCode() == Daemon.SUCCESS) {
                logger.info("Received metadata msg: " + result.getMessage());
            }
           
            logger.info("Received metadata response: " + result.getReturnCode());
                

        } catch (UnknownHostException e) {
                logger.error("Don't know about host " + this.serverHostIpAddress);
        } catch (IOException e) {
                logger.error("Can not connect to Lift client at [" + this.serverHostIpAddress + ":" + this.serverHostPort + "].");
        } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException found!");
        }        
        
        return result;
    }
    
    
    private Result retrieveFileFromRemoteClient(String remoteHostname, int remotePort, String fileID, String fileName) {
        
        Result result       = new Result();
        boolean isFileSaved = false;
        long bytesReceived  = 0;
        
        try (// localSock: used for sending progress
             Socket progressBarSock             = new Socket("localhost", sem.progressBarServerPort);
             ObjectOutputStream progressBarOut  = new ObjectOutputStream(progressBarSock.getOutputStream());
                
            // remoteSock: used for getting file server port
             Socket retrieveOperationSock            = new Socket(remoteHostname, remotePort);
             ObjectOutputStream retrieveOperationOut = new ObjectOutputStream(retrieveOperationSock.getOutputStream());
             ObjectInputStream remoteOperationOut    = new ObjectInputStream(retrieveOperationSock.getInputStream());
        ) {
            
            // ------------------------------------------------------------------------------------------------------------
            //  Retrieve operation to obtain the file provider port
            // ------------------------------------------------------------------------------------------------------------
            logger.info("Attempting to initiate a RETRIEVE operation with a remote server.");
            Transaction transaction = new Transaction(Operation.RETRIEVE, new String []{fileID});
            retrieveOperationOut.writeObject(transaction);
            
            // Read fileprovider port
            Result retrieveResult = (Result)remoteOperationOut.readObject();
            
            logger.info(String.format("RETRIEVE response: statusCode=%s, message=%s",retrieveResult.getReturnCode(),retrieveResult.getMessage()));
            
            int fileProviderPort = retrieveResult.getReturnCode();
            
            if( fileProviderPort == -1){
                // error
                logger.error("File provider server was not started");
            }
            
            logger.info("File provider server was started. Listening remotely on port " + fileProviderPort);
            
            // ------------------------------------------------------------------------------------------------------------
            
            
            String downloadedFileName = CommonUtility.generateGUID(0, 8) + "-" + fileName;
            File writeLocation = new File(Daemon.sharedDirFile.getAbsolutePath() + File.separator + downloadedFileName);
            // ------------------------------------------------------------------------------------------------------------
            //  BEGIN: Transmission of data
            // ------------------------------------------------------------------------------------------------------------              
            
            try(Socket fileTransferSock            = new Socket(remoteHostname, fileProviderPort);
                ObjectOutputStream fileTransferOut = new ObjectOutputStream(fileTransferSock.getOutputStream());
                InputStream  fileTransferIn        = fileTransferSock.getInputStream();
                FileOutputStream fos               = new FileOutputStream(writeLocation);){
                
                
                logger.info(String.format("Opening connection to file provider server %s and port %d", remoteHostname, fileProviderPort));
          
                // Read actual bytes from remote client
                byte[] buffer = new byte[8192]; // 100 kb
                long readBytes;            
                long remainingBytes = totalBytes;
                
                while ( (readBytes = fileTransferIn.read(buffer, 0, Math.min(buffer.length, (int)remainingBytes))) > 0){
                    
                    if(readBytes >= 0){
                        bytesReceived = bytesReceived + readBytes;
                        remainingBytes = remainingBytes - readBytes;                                        
                    }

                    // report to client launcher the bytes read for progress bar
                    if(progressBarSock.isConnected()){
                        progressBarOut.writeLong(readBytes);
                    }

                    //logger.info("Received from client [" + remoteHostname + "] " + readBytes + " bytes.");
                    //logger.info(String.format("progress [%d/%d] - remaining: %d", bytesReceived, totalBytes, remainingBytes));
                    
                    // write buffer to filesystem
                    try {
                        fos.write(buffer, 0, (int)readBytes);
                        isFileSaved = true;
                    } catch (IOException e) {
                        isFileSaved = false;
                       break;
                    }
                }
                fos.flush();
                logger.info("Total bytes received: " + bytesReceived);
                progressBarOut.flush();
                
                if (isFileSaved) {
                    result.setReturnCode(0);
                    result.setResult(Daemon.sharedDirFile.getAbsolutePath() + File.pathSeparator + downloadedFileName);
                } else {
                    result.setReturnCode(1);
                    result.setMessage("Daemon: File " + Daemon.sharedDirFile.getAbsolutePath() +
                            File.pathSeparator + downloadedFileName + " could not be saved.");
                }                
                
            }catch (IOException e) {
                logger.error("Can not connect to remote file provider server  at [" + remoteHostname + ":" + fileProviderPort + "]");
                result.setReturnCode(2);
                result.setMessage("Daemon: Cannot connect to remote peer for file download");
            } 
            // ------------------------------------------------------------------------------------------------------------  
            
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Can not connect to remote Lift client at [" + remoteHostname + ":" + remotePort + "]");
            result.setReturnCode(2);
            result.setMessage("Daemon: Can not connect to remote Lift client. Client might not be available.");
        } 
        
        return result;
    }

    @Override
    public void run() {
        Result result       = new Result();
        
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];
        
        // VERY IMPORTANT: do not start the download unless a local connection is made and client is ready
        while (sem.isReady == false) {
            // Wait until the local client is ready start the handling the download...
        }
        logger.info("Finished waiting...");
        if(sem.terminate) return;
                      
        Gson gson = new Gson();
        Type type = new TypeToken<RepositoryFile>() {}.getType();
        RepositoryFile repositoryFile = gson.fromJson(metadataJson, type);

        File f = new File(repositoryFile.getName());
        String fileName = f.getName();

        totalBytes = repositoryFile.getSize();

        // This will start sending longs (for progress bar) to client (which now is acting like a server)
        result = retrieveFileFromRemoteClient(this.serverHostIpAddress, this.serverHostPort, fileID, fileName);

    }
    
}
