package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
import com.lift.daemon.RepositoryDAO;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;
import java.io.File;

/**
 * This class handles the Lift UFL operation:
 * 
 * $ lift ufl <fileID>
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class UflCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(UflCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
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
            logger.info("Generating the UFL for file ID: " + fileID);
            
            ufl = CommonUtility.encodeUFL(sessionDatabase.getSession().getGUID(), fileID);
            
            logger.info("The UFL is: " + ufl);
            result = new Result(0, null, ufl);
            
        } else {
            
            logger.error("Failed to get the UFL. No such ID: " + fileID);
            result =  new Result(1, "Daemon: Failed to get the UFL. No such ID: " + fileID, null);
        }
        
        return result;
    }
}
