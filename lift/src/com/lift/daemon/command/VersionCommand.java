package com.lift.daemon.command;

import com.lift.common.Logger;
import com.lift.daemon.Result;
import java.util.Calendar;


public class VersionCommand implements LiftCommand {
    
    private static final Logger logger  = new Logger(VersionCommand.class);
    
    // TODO: remove this hardcoded values
    private static final String SERVER_VERSION       = "1.0.0";
    private static final String SERVER_BUILD_DATE    = Calendar.getInstance().getTime().toString();

    public VersionCommand() {
    }
    
    @Override
    public Result execute() {
        Result result = new Result();
        
        logger.info("Program version requested.");
        result = new Result(0, null, new String[] {SERVER_VERSION, SERVER_BUILD_DATE});
        
        return result;
    }
}
