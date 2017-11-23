package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.daemon.FileProviderServer;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.Result;
import java.io.File;
import java.net.ServerSocket;

/**
 * This class handles the internal RETRIEVE operation.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class RetrieveCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(RetrieveCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;

    public RetrieveCommand(String fileID, RepositoryDAO repositoryDatabase) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
    }

    @Override
    public Result execute() {
        
        Result result       = new Result();
        int fileServerPort  = startFileProviderServer();
        
        if(-1 == fileServerPort){
            logger.error("Unable to start the file server");
            result.setMessage("Error: Unable to start the file server for data transmission.");
            
        } else{
            logger.info(String.format("It seems that the remote file provider was opened in port %d", fileServerPort));
            result.setMessage("Success setting the file server.");
        }
        
        result.setReturnCode(fileServerPort);

        return result;
    }
    
    
    
    public int startFileProviderServer(){
        
        int fileProviderServerPort  = -1;
        String filePath = repositoryDatabase.getFileByID(fileID).getName();
        
        // increment the hits by one
        repositoryDatabase.getFilesMap().get(fileID).incrementHits(1);
        repositoryDatabase.commit();

        try{
            ServerSocket serverSocket = new ServerSocket(0);
            fileProviderServerPort    = serverSocket.getLocalPort();            
            
            Thread fileProviderServerThread = new Thread(new FileProviderServer(serverSocket, filePath));
            fileProviderServerThread.start();
            
            logger.info("Spawned fileserver socket server. Listening in port: " + fileProviderServerPort);
            
        } catch(Exception ex){
            logger.error("Exception caught on startFileProviderServer");
            ex.printStackTrace();
        }

        return fileProviderServerPort;
    }
}
