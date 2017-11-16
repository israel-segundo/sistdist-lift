package com.lift.daemon.command;

import com.lift.common.CommonUtility;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;
import java.util.Base64;


public class UflCommand implements LiftCommand {
    
    private RepositoryDAO repositoryDatabase  = null;
    private SessionDAO sessionDatabase        = null;
    private String fileID                     = null;

    public UflCommand(String fileID, RepositoryDAO repositoryDatabase, SessionDAO sessionDatabase) {
        this.fileID             = fileID;
        this.repositoryDatabase = repositoryDatabase;
        this.sessionDatabase    = sessionDatabase;
    }
    
    @Override
    public Result execute() {
        String ufl    = null;
        Result result = new Result();
        
        repositoryDatabase.reload();
        
        if(repositoryDatabase.getFilesMap().keySet().contains(fileID)) {
            System.out.println("[ INFO ] UFL-CMD: Generating the UFL for file ID: " + fileID);
            
            ufl = CommonUtility.encodeUFL(sessionDatabase.getSession().getGUID(), fileID);
            
            System.out.println("[ INFO ] UFL-CMD: The UFL is: " + ufl);
            result = new Result(0, null, ufl);
            
        } else {
            
            System.out.println("[ ERROR ] UFL-CMD: Failed to get the UFL. No such ID: " + fileID);
            result =  new Result(1, "Daemon: Failed to get the UFL. No such ID: " + fileID, null);
        }
        
        return result;
    }
}
