package com.lift.daemon;

import com.google.gson.Gson;
import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.common.Operation;
import com.lift.daemon.command.AddCommand;
import com.lift.daemon.command.FilesCommand;
import com.lift.daemon.command.GetCommand;
import com.lift.daemon.command.IdCommand;
import com.lift.daemon.command.RmCommand;
import com.lift.daemon.command.ShareCommand;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.lift.daemon.command.LiftCommand;
import com.lift.daemon.command.MetaCommand;
import com.lift.daemon.command.RetrieveCommand;
import com.lift.daemon.command.UflCommand;
import com.lift.daemon.command.VersionCommand;
import java.io.File;

public class DaemonTask implements Runnable {
    
    private static final Logger logger  = new Logger(DaemonTask.class, AppConfig.logFilePath + File.separator + "lift.log");
    
    Socket sock;
    RepositoryDAO repositoryDB;
    SessionDAO sessionDB;
    volatile Daemon.Sem sem;
    
    public DaemonTask(Daemon.Sem sem, Socket sock, RepositoryDAO repositoryDB, SessionDAO sessionDB){
        this.sock         = sock;
        this.repositoryDB = repositoryDB;
        this.sessionDB    = sessionDB;
        this.sem          = sem;
    }
    
    @Override
    public void run() {
        
        try (
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
            ) 
        {    
            Transaction transaction = (Transaction) in.readObject();
            logger.info("Petition received: " + transaction.getOperation().toUpperCase());
                
            Result result = attendTransaction(transaction);
            
            out.writeObject(result);
            
            logger.info(Thread.currentThread().getName() + ": Data sent to client.");
            
        } catch (ClassNotFoundException | IOException ex) {
            logger.error("Could not process the petition received from client.");
        }        
    }
    
    private Result attendTransaction(Transaction transaction){
        
        LiftCommand command  = null;
        String [] parameters = transaction.getParameters();
        
        String firstParameter = (parameters==null) ? "" : parameters[0];
        
        switch(transaction.getOperation()){
            
            case Operation.ADD:
                command = new AddCommand(firstParameter, repositoryDB);
                break;
                
            case Operation.FILES:
                command = new FilesCommand(repositoryDB);
                break;
                
            case Operation.GET:
                
                logger.info("Sem from DAEMONTASK hashcode: " + sem.hashCode());
                GetCommand getCommand = new GetCommand(firstParameter, sem);
                Result result         = getCommand.getMetadataFromRemoteClient(firstParameter);
                
                if (result.getReturnCode() != Daemon.SUCCESS) {
                    Daemon.terminateDownload = true;
                    sem.terminate = Boolean.getBoolean(firstParameter);
                    return result;
                }
                
                Gson gson = new Gson();
                String metaJson = gson.toJson(result.getResult());
                logger.info("Metadata is: " + metaJson);
                getCommand.setMetadata(metaJson);
                
                Thread downloadFile = new Thread(getCommand);
                downloadFile.start();
                
                logger.info("PID DAEMONTASK: " + downloadFile.getId());
                
                return result;
                
            case Operation.CLIENT_READY:
                String clientReady        = parameters[0];
                String progressBarServerPort       = parameters[1];
                Daemon.isClientReady      = Boolean.valueOf(clientReady);
                Daemon.progressBarServerPort = Integer.parseInt(progressBarServerPort);
                
                sem.isReady = Boolean.valueOf(clientReady);
                sem.progressBarServerPort = Integer.parseInt(progressBarServerPort);
                
                logger.info("isClientReady: " + sem.isReady);
                logger.info("progressBarServerPort: " + sem.progressBarServerPort);
                
                return new Result();
                
            case Operation.TERMINATE_DOWNLOAD:
                
                Daemon.terminateDownload = Boolean.valueOf(firstParameter);
                sem.terminate = Boolean.valueOf(firstParameter);
                
                return new Result();
                
            case Operation.META:
                command = new MetaCommand(firstParameter, repositoryDB);
                break;    
                
            case Operation.RETRIEVE:
                // TODO:  Extends retrieve functionality
                
                command = new RetrieveCommand(firstParameter, repositoryDB, sock);
                break;
                
            case Operation.ID:
                command = new IdCommand(sessionDB);
                break;    
                
            case Operation.RM:
                command = new RmCommand(firstParameter, repositoryDB);
                break;    
                
            case Operation.SHARE:
                command = new ShareCommand(firstParameter, repositoryDB, sessionDB);
                break;
                
            case Operation.UFL:
                command = new UflCommand(firstParameter, repositoryDB, sessionDB);
                break;    
                
            case Operation.VERSION:
                command = new VersionCommand();
                break;    
                              
        }
        
        return command.execute();
    }    
}
