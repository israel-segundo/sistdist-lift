package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import static com.lift.daemon.Daemon.sem;
import com.lift.daemon.DaemonTask;
import com.lift.daemon.FileProviderServer;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class RetrieveCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(RetrieveCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
    private String fileID                    = null;
    private RepositoryDAO repositoryDatabase = null;
    private Socket sock                      = null;

    public RetrieveCommand(String fileID, RepositoryDAO repositoryDatabase, Socket sock) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
        this.sock               = sock;
    }

    @Override
    public Result execute() {
        
        Result result       = new Result();
        int fileServerPort  = startFileProviderServer();
        
        if(-1 == fileServerPort){
            logger.error("Unable to start the file server");
            result.setMessage("error");
            
        } else{
            logger.info(String.format("It seems that the remote file provider was opened in port %d", fileServerPort));
            result.setMessage("success");
        }
        
        result.setReturnCode(fileServerPort);

        return result;
    }
    
    
    
    public int startFileProviderServer(){
        
        int fileProviderServerPort  = -1;
        String filePath = repositoryDatabase.getFileByID(fileID).getName();

        try{
            ServerSocket serverSocket   = new ServerSocket(0);
            fileProviderServerPort      = serverSocket.getLocalPort();            
            
            Thread fileProviderServerThread = new Thread(new FileProviderServer(serverSocket,filePath));
            fileProviderServerThread.start();
            
            logger.info("Spawned fileserver socket server. Listening in port: " + fileProviderServerPort);
            
        } catch(Exception ex){
            logger.error("Exception caught on  startFileProviderServer");
            ex.printStackTrace();
        }

        return fileProviderServerPort;
    }
}
