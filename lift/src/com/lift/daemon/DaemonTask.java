
package com.lift.daemon;

import com.lift.daemon.command.AddCommand;
import com.lift.daemon.command.Command;
import com.lift.daemon.command.FilesCommand;
import com.lift.daemon.command.ShareCommand;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DaemonTask implements Runnable{
    
    Socket sock;
    RepositoryDAO database;
    
    public DaemonTask(Socket sock, RepositoryDAO database){
        this.sock       = sock;
        this.database   = database;
    }
    
    @Override
    public void run() {
        
        try (
                ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());) {
            
            Transaction transaction = (Transaction) in.readObject();
       
            Result result = attendTransaction(transaction);
            
            out.writeObject(result);
            
            System.out.println("[ INFO ] " + Thread.currentThread().getName() + ": File sent to client.");
            
        } catch (IOException e) {
            e.printStackTrace();
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DaemonTask.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private Result attendTransaction(Transaction transaction){
        
        Command command  = null;
        
        switch(transaction.getOperation()){
            case "files":
                command = new FilesCommand(database);
                break;
                
            case "add":
                command = new AddCommand(transaction.getParameter(),database);
                break;
                
             case "share":
                command = new ShareCommand(transaction.getParameter());
                break;    
             case "get":
               // command = new GetCommand(transaction.getParameter());
                break;                  
        }
            
            
        return command.execute();
    }    
}
