package com.lift.daemon.command;

import com.lift.common.Logger;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;


public class IdCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(IdCommand.class);
    
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
