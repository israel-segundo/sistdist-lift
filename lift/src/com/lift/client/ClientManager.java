package com.lift.client;

import com.lift.daemon.Daemon;

/**
 * This class handles the client operations received by the Client Launcher.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientManager {
    
    private static final String CLIENT_VERSION      = "1.0.0";
    private static final String CLIENT_BUILD_DATE   = "Thursday 7 Nov, 2017";
    private static final String SERVER_VERSION      = "1.0.0";
    private static final String SERVER_BUILD_DATE   = "Thursday 7 Nov, 2017";
    
    
    private final String repoFileRoute    = "repo.json";
    private final String sharedDirRoute   = "/scratch/lift/";
    private final String daemonPort       = "45115";
    
    // TODO: replace this with IPC for daemon-clientManager
    private Daemon daemon = new Daemon();
    
    public void add(String filePath) {
        daemon.add(filePath);
    }
    
    public void files() {
        daemon.files();
    }
    
    public void get(String UFL) {
    }
    
    public void id() {
        daemon.id();
    }
    
    public void rm(String fileID) {
        daemon.rm(fileID);
    }
    
    public void share(String filePath) {
    }
    
    public void ufl(String fileID) {
        daemon.ufl(fileID);
    }
    
    public void version() {
        System.out.printf("Client:\n");
        System.out.printf(" %-15s%s\n", "Version:", CLIENT_VERSION);
        System.out.printf(" %-15s%s\n", "Built:",   CLIENT_BUILD_DATE);
        // TODO: obtain below properties from server
        System.out.printf("\nServer:\n");
        System.out.printf(" %-15s%s\n", "Version:", SERVER_VERSION);
        System.out.printf(" %-15s%s\n", "Built:",   SERVER_BUILD_DATE);
        System.out.printf("\n");
    }
}
