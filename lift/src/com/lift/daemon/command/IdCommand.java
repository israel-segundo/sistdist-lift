package com.lift.daemon.command;

import com.lift.common.AppConfig;
import com.lift.common.Logger;
import com.lift.daemon.Daemon;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;
import java.io.File;


public class IdCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(IdCommand.class, AppConfig.logFilePath + File.separator + "lift.log");
    
    private SessionDAO sessionDatabase  = null;

    public IdCommand(SessionDAO sessionDatabase) {
        this.sessionDatabase    = sessionDatabase;
    }
    
    @Override
    public Result execute() {
        Result result = new Result();
        
        sessionDatabase.reload();
        
        String id = sessionDatabase.getSession().getGUID();
        logger.info("The user ID is: " + id);
        
        result = new Result(0, null, sessionDatabase.getSession());
        
        return result;
    }
}
