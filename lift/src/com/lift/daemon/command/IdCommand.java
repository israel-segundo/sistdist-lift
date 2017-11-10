package com.lift.daemon.command;

import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;


public class IdCommand implements LiftCommand {
    private SessionDAO sessionDatabase  = null;

    public IdCommand(SessionDAO sessionDatabase) {
        this.sessionDatabase    = sessionDatabase;
    }
    
    @Override
    public Result execute() {
        Result result = new Result();
        
        sessionDatabase.reload();
        
        String id = sessionDatabase.getSession().getGUID();
        System.out.println("[ INFO ] ID-CMD: The user ID is: " + id);
        result = new Result(0, null, id);
        
        return result;
    }
}
