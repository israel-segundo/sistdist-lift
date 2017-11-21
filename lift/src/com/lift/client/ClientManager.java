package com.lift.client;

import com.lift.common.AppConfig;
import com.lift.common.CommonUtility;
import com.lift.common.Logger;
import com.lift.common.Operation;
import com.lift.common.ProgressBar;
import com.lift.daemon.Daemon;
import com.lift.daemon.RepositoryFile;
import com.lift.daemon.Result;
import com.lift.daemon.SessionDAO;
import com.lift.daemon.SessionFile;
import com.lift.daemon.Transaction;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class handles the client operations received by the Client Launcher.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientManager {
    
    private static Logger logger                = null;
    private static String clientVersion         = "1.0.0";
    private static String clientBuildDate       = Calendar.getInstance().getTime().toString();
    private static final int SUCCESS            = 0;
    private int daemonPort                      = 0;
    private static String daemonHostname        = "localhost";
    private static SessionDAO sessionDatabase   = null;
    
    private AppConfig appConfig  = null;
    
    public ClientManager() {
        
        loadConfig();
        loadSession();
        
    }
    
    private void loadConfig(){
    
        appConfig = new AppConfig();
        
        clientVersion   = appConfig.getProperty("lift.build.version", clientVersion);
        daemonHostname  = appConfig.getProperty("lift.daemon.hostname", daemonHostname);
        clientBuildDate = appConfig.getProperty("lift.build.date", clientBuildDate);
        
        AppConfig.logFilePath = appConfig.getProperty("lift.config.dir");
        logger = new Logger(ClientManager.class, AppConfig.logFilePath + File.separator + "lift.log");
    }
    
    private void loadSession(){
        
        logger.info("Loading session from disk...");
        String liftConfigLocation   = appConfig.getProperty("lift.config.dir", System.getProperty("java.io.tmpdir"));
        String sessionFileLocation  = liftConfigLocation + File.separator + "session.json";

        try{
            
            logger.info("Target session database location: " + sessionFileLocation);
            
            sessionDatabase = new SessionDAO(new File(sessionFileLocation));
            sessionDatabase.reload();
            
            logger.info("Session database loaded.");
            logger.info(appConfig.toString());
            
            daemonPort = sessionDatabase.getSession().getDaemonPort();
            
        }catch(Exception ex){

            logger.error("Error at loading session database");
            
        }
    }
    
    private Result sendOperationToDaemon(Transaction transaction){
        
        Result result = null;

        logger.info("Trying to establish a connection to the daemon");

        try (Socket sock = new Socket(daemonHostname, daemonPort);
             ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        ) {
                // Send
                out.writeObject(transaction);
                
                // Wait and Receive Result
                result = (Result) in.readObject();

                logger.info("Received result from daemon: " + result);
                
        } catch (UnknownHostException e) {
                logger.error("Don't know about host " + daemonHostname);
                System.exit(1);
        } catch (IOException e) {
                System.err.println("Can not connect to Lift daemon at [" + daemonHostname + ":" + daemonPort + "]. Is the Lift daemon running?");
                System.exit(1);
        } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException found!");
                System.exit(1);
        }        
        
        return result;
    }
    
    public void files() {
        
        Transaction transaction = new Transaction(Operation.FILES, null);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            
            // TODO: sort the list by date added
            Map<String, RepositoryFile> map = (HashMap<String, RepositoryFile>)result.getResult();
            Set<Map.Entry<String, RepositoryFile>> fileSet = map.entrySet();
            StringBuilder sb = new StringBuilder();

            String format = CommonUtility.getFormatForFilesCmd(map);
            sb.append( String.format(format, "LOCATION", "FILE ID", "DATE ADDED", "SIZE", "HITS") );
            

            fileSet.forEach((entry) -> {
                String location     = entry.getValue().getName();
                String size         = CommonUtility.humanReadableByteCount(entry.getValue().getSize(), false);
                String fileID       = entry.getValue().getGUID();
                String dateAdded    = CommonUtility.humanReadableDaysAgo(entry.getValue().getDateAdded());
                String hits         = String.valueOf(entry.getValue().getHits());
                sb.append( String.format(format, location, fileID, dateAdded, size, hits) );
            });

            System.out.println(sb.toString());
            
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void add(String filePath) {
        
        Transaction transaction = new Transaction(Operation.ADD, new String [] {filePath});
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            // Do nothing
        } else {
            System.out.println(result.getMessage());
        }
    }

    public void get(String ufl) {
        
        if(! CommonUtility.isUFLValid(ufl)) {
            System.out.println("The UFL is invalid. Please provide a valid UFL.");
            return;
        }

        // Decode the UFL
        String[] decodedUFL = CommonUtility.decodeUFL(ufl);
        String clientGUID   = decodedUFL[0];
        String fileID       = decodedUFL[1];       
        
        // This is a kind of semaphore to avoid infinite loop in GetCommand
        sendOperationToDaemon(new Transaction(Operation.CLIENT_READY, new String [] {"false"}));
        sendOperationToDaemon(new Transaction(Operation.TERMINATE_DOWNLOAD, new String [] {"false"}));
        
        Transaction getTransaction = new Transaction(Operation.GET, new String [] {ufl});
        Result metadata            = sendOperationToDaemon(getTransaction);

        if (metadata.getReturnCode() != SUCCESS) {
            System.out.println(metadata.getMessage());
            sendOperationToDaemon(new Transaction(Operation.TERMINATE_DOWNLOAD, new String [] {"false"}));
            return;
        }
        
        RepositoryFile file = (RepositoryFile) metadata.getResult();
        long totalSize = file.getSize();

        // If metadata is successful this means the download has been started by a daemon thread
        // Start reading from the socket the longs sent by daemon for progress bar
        readDownloadProgressFromDaemon(totalSize);
        
        sendOperationToDaemon(new Transaction(Operation.CLIENT_READY, new String [] {"false"}));
        
    }
    
    public void id() {
        Transaction transaction = new Transaction(Operation.ID, null);
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            SessionFile session = (SessionFile) result.getResult();
            System.out.format("%-10s%-35s\n", "User GUID is: ", session.getGUID());
            boolean isConnected = session.getIsConnected();
            String state = (isConnected) ? 
                            Logger.ANSI_GREEN + "CONNECTED" + Logger.ANSI_RESET : 
                            Logger.ANSI_RED + "NOT CONNECTED" + Logger.ANSI_RESET;
            System.out.format("\nYou are [ %s ] to the Lift network.\n", state);
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void rm(String fileID) {
        Transaction transaction = new Transaction(Operation.RM, new String []{fileID});
        Result result           = sendOperationToDaemon(transaction);
        
        if(result.getReturnCode() == SUCCESS) {
            System.out.println(result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void share(String filePath) {
        Transaction transaction = new Transaction(Operation.SHARE,  new String []{filePath});
        Result result           = sendOperationToDaemon(transaction);

        if (result.getReturnCode() == SUCCESS) {
            System.out.println(result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void ufl(String fileID) {
        Transaction transaction = new Transaction(Operation.UFL, new String[]{fileID});
        Result result           = sendOperationToDaemon(transaction);

        if (result.getReturnCode() == SUCCESS) {
            System.out.printf("%-10s%-30s\n", "UFL is:", result.getResult());
        } else {
            System.out.println(result.getMessage());
        }
    }
    
    public void version() {
        Transaction transaction = new Transaction(Operation.VERSION, null);
        Result result           = sendOperationToDaemon(transaction);
        String[] serverValues   = (String[])result.getResult();
        
        System.out.printf("Client:\n");
        String format = " %-15s%s\n";
        System.out.printf(format, "Version:", clientVersion);
        System.out.printf(format, "Built:",   clientBuildDate);
        System.out.printf("\nServer:\n");
        System.out.printf(" %-15s%s\n", "Version:", serverValues[0]);
        System.out.printf(" %-15s%s\n", "Built:",   serverValues[1]);
        System.out.printf("\n");
    }
    
    public boolean readDownloadProgressFromDaemon(long totalDataSize) {
        boolean isErrorPresent = false;
        long delta             = 0;
        ProgressBar bar        = new ProgressBar(totalDataSize, "Downloading");
        
        // TODO:  VERY IMPORTANT Implement timeout mech
        ServerSocket sock;
        try {
            sock = new ServerSocket(daemonPort);
            
            while (true) {

                Socket clientSocket = sock.accept();
                try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());) {
                    
                    Daemon.localClientSocket = clientSocket;
                    sendOperationToDaemon(new Transaction(Operation.CLIENT_READY, new String [] {"true"}));
                    
                    delta = in.readLong();
                    bar.updateProgress(delta);

                    if (delta >= totalDataSize) {
                        System.out.printf("\n\nDownload is complete.\n");
                        break;
                    }

                } catch (IOException ex) {
                    isErrorPresent = true;
                    sendOperationToDaemon(new Transaction(Operation.CLIENT_READY, new String [] {"false"}));
                    System.out.printf("\n\nError: The connection was interrupted.\n");
                    break;
                }
            }
        } catch (IOException ex) {
            
        }
        
        
        return isErrorPresent;
    }
}
