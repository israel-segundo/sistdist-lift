package com.lift.client;

import com.lift.daemon.Daemon;

/**
 * This class handles the client operations received by the Client Launcher.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientManager {
    
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
        daemon.version();
    }
}
