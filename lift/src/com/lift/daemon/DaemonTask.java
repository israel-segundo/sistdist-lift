package com.lift.daemon;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lift.daemon.command.LiftCommand;
import com.lift.daemon.command.MetaCommand;
import com.lift.daemon.command.RetrieveCommand;
import com.lift.daemon.command.UflCommand;
import com.lift.daemon.command.VersionCommand;

public class DaemonTask implements Runnable {
    
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
            System.out.println("[ INFO ] Petition received: " + transaction.getOperation().toUpperCase());
                
            Result result = attendTransaction(transaction);
            
            out.writeObject(result);
            
            System.out.println("[ INFO ] " + Thread.currentThread().getName() + ": Data sent to client.");
            
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(DaemonTask.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private Result attendTransaction(Transaction transaction){
        
        LiftCommand command  = null;
        
        switch(transaction.getOperation()){
            
            case Operation.ADD:
                command = new AddCommand(transaction.getParameter(), repositoryDB);
                break;
                
            case Operation.FILES:
                command = new FilesCommand(repositoryDB);
                break;
                
            case Operation.GET:
                //command = new GetCommand(transaction.getParameter(), sock);
                break;
                
            case Operation.META:
                command = new MetaCommand(transaction.getParameter(), repositoryDB);
                break;    
                
            case Operation.RETRIEVE:
                //command = new RetrieveCommand(transaction.getParameter(), repositoryDB);
                break;
                
            case Operation.ID:
                command = new IdCommand(sessionDB);
                break;    
                
            case Operation.RM:
                command = new RmCommand(transaction.getParameter(), repositoryDB);
                break;    
                
            case Operation.SHARE:
                command = new ShareCommand(transaction.getParameter(), repositoryDB, sessionDB);
                break;
                
            case Operation.UFL:
                command = new UflCommand(transaction.getParameter(), repositoryDB, sessionDB);
                break;    
                
            case Operation.VERSION:
                command = new VersionCommand();
                break;    
                              
        }
            
        return command.execute();
    }    
}
