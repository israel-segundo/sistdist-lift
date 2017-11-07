package com.lift.client;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class handles the Lift client operations as the end user will use.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class Client {
    
    private static final String CLIENT_VERSION = "1.0.0";
    private static final String CLIENT_BUILD_DATE = "Thursday 7 Nov, 2017";
    private static final String SERVER_VERSION = "1.0.0";
    private static final String SERVER_BUILD_DATE = "Thursday 7 Nov, 2017";
    
    /*
     *  Client operations constants
     */
    private static final String ADD_FILE_TO_REPO_CMD        = "add";
    private static final String LIST_FILES_IN_REPO_CMD      = "files";
    private static final String GET_FILE_FROM_UFL_CMD       = "get";
    private static final String SHOW_USER_SESSION_ID_CMD    = "id";
    private static final String REMOVE_FILE_FROM_REPO_CMD   = "rm";
    private static final String SHARE_FILE_CMD              = "share";
    private static final String GET_UFL_FROM_FILE_CMD       = "ufl";
    private static final String SHOW_VERSION_INFO_CMD       = "version";
    
    private static final String SHOW_HELP_OPT               = "--help";
    
    private static final Map<String, String> commandDescriptions = new LinkedHashMap<>();
    private static final Map<String, String> optionDescriptions  = new LinkedHashMap<>();
    
    
    public static void initOptions() {
        commandDescriptions.put(ADD_FILE_TO_REPO_CMD,       "Add a file to local repository");
        commandDescriptions.put(LIST_FILES_IN_REPO_CMD,     "List files");
        commandDescriptions.put(GET_FILE_FROM_UFL_CMD,      "Get the file from remote repository");
        commandDescriptions.put(SHOW_USER_SESSION_ID_CMD,   "Show the user GUID in the network");
        commandDescriptions.put(REMOVE_FILE_FROM_REPO_CMD,  "Remove a file from local repository");
        commandDescriptions.put(SHARE_FILE_CMD,             "Share a file and generate it's UFL");
        commandDescriptions.put(GET_UFL_FROM_FILE_CMD,      "Generate the file's UFL");
        commandDescriptions.put(SHOW_VERSION_INFO_CMD,      "Show the Lift version information");
        
        optionDescriptions.put(SHOW_HELP_OPT,               "Print usage");
        
    }

    public static void main(String[] args) {
        
        initOptions();
        
        // 
        // Parser
        //
        for (String opt : args) {
        
            switch(opt) {
                
                case SHARE_FILE_CMD:
                    checkOptArgsNumber(SHARE_FILE_CMD, args, 2);
                    break;
                
                case LIST_FILES_IN_REPO_CMD:
                    // do list files
                    break;
                
                case REMOVE_FILE_FROM_REPO_CMD:
                    checkOptArgsNumber(REMOVE_FILE_FROM_REPO_CMD, args, 2);
                    break;
                    
                case ADD_FILE_TO_REPO_CMD:
                    checkOptArgsNumber(ADD_FILE_TO_REPO_CMD, args, 2);
                    break;
                
                case SHOW_USER_SESSION_ID_CMD:
                    // do show user session id
                    break;
                
                case GET_FILE_FROM_UFL_CMD:
                    checkOptArgsNumber(GET_FILE_FROM_UFL_CMD, args, 2);
                    break;
                case GET_UFL_FROM_FILE_CMD:
                    checkOptArgsNumber(GET_UFL_FROM_FILE_CMD, args, 2);
                    break;
                
                case SHOW_VERSION_INFO_CMD:
                    showVersion();
                    break;
                
                case SHOW_HELP_OPT:
                    showUsage();
                    break;
                
                default:
                    System.out.println("lift: '" + opt + "' is not a lift command.");
                    System.out.println("See 'lift --help'");
                    break;
            
            }
        }
    }
    
    private static void showUsage() {
        System.out.printf("\nUsage:  lift COMMAND\n\n");
        System.out.printf("A P2P client for lightweight file transfer\n\n");
        
        System.out.printf("Options:\n\n");
        
        optionDescriptions.keySet().forEach((option) -> {
            System.out.printf("  %-10s%s\n", option, optionDescriptions.get(option));
        });
        
        System.out.printf("\n");
        
        System.out.printf("Commands:\n\n");
        
        commandDescriptions.keySet().forEach((command) -> {
            System.out.printf("  %-10s%s\n", command, commandDescriptions.get(command));
        });
        
        System.out.printf("\n");
    }
    
    private static void showVersion() {
        System.out.printf("Client:\n");
        System.out.printf(" %-15s%s\n", "Version:", CLIENT_VERSION);
        System.out.printf(" %-15s%s\n", "Built:", CLIENT_BUILD_DATE);
        System.out.printf("\nServer:\n");
        System.out.printf(" %-15s%s\n", "Version:", SERVER_VERSION);
        System.out.printf(" %-15s%s\n", "Built:", SERVER_BUILD_DATE);
        System.out.printf("\n");
    }
    
    private static void checkOptArgsNumber(String opt, String[] args, int requiredArgsNumber) {
        if (args.length != requiredArgsNumber) {
            System.out.println("'lift " + opt + "' requires exactly " + (requiredArgsNumber - 1) + " argument");
            System.out.println("See 'lift --help'");
            System.exit(0);
        }
    }
}
