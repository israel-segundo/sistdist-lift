package com.lift.daemon;

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

public class DaemonTask implements Runnable {
    
    private static final Logger logger  = new Logger(DaemonTask.class);
    
    Socket sock;
    RepositoryDAO repositoryDB;
    SessionDAO sessionDB;
    
    public DaemonTask(Socket sock, RepositoryDAO repositoryDB, SessionDAO sessionDB){
        this.sock         = sock;
        this.repositoryDB = repositoryDB;
        this.sessionDB    = sessionDB;
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
    
    private void retrieveFile(Transaction transaction){
        
    }
    
    private Result retrieveMeta(Transaction transaction){
        return attendTransaction(transaction);
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
                
                if(null != transaction && null != transaction.getParameters()){
                    
                    String ufl           = parameters[0];
                    String metadataJson  = parameters[1];
                    
                    return new GetCommand(ufl, metadataJson, sock).execute();

                }
                break;
                
            case Operation.META:
                command = new MetaCommand(firstParameter, repositoryDB);
                break;    
                
            case Operation.RETRIEVE:
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
