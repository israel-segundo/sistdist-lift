package com.lift.daemon.command;

import com.lift.daemon.Result;

/**
 * This interface specifies the Lift operation contract.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public interface LiftCommand {

    public Result execute();
}
