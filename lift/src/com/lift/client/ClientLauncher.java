package com.lift.client;

import com.lift.common.LiftOpt;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class handles the Lift client operations as the end user will use.
 * 
 * @author Alejandro Garcia
 * @author Israel Segundo
 */
public class ClientLauncher {
    
    private static final Map<String, String> commandDescriptions = new LinkedHashMap<>();
    private static final Map<String, String> optionDescriptions  = new LinkedHashMap<>();
    
    
    public static void initOptions() {
        
        commandDescriptions.put(LiftOpt.ADD,      "Add a file to local repository");
        commandDescriptions.put(LiftOpt.FILES,    "List files");
        commandDescriptions.put(LiftOpt.GET,      "Get the file from remote repository");
        commandDescriptions.put(LiftOpt.ID,       "Show the user GUID in the network");
        commandDescriptions.put(LiftOpt.RM,       "Remove a file from local repository");
        commandDescriptions.put(LiftOpt.SHARE,    "Share a file and generate it's UFL");
        commandDescriptions.put(LiftOpt.UFL,      "Generate the file's UFL");
        commandDescriptions.put(LiftOpt.VERSION,  "Show the Lift version information");
        
        optionDescriptions.put(LiftOpt.HELP,      "Print usage");
        
    }

    public static void main(String[] args) {
        
        initOptions();
        
        ClientManager client = new ClientManager();
        // 
        // Parser
        //
        loop: for (String opt : args) {
        
            switch(opt) {
                
                case LiftOpt.SHARE:
                    checkOptArgsNumber(LiftOpt.SHARE, args, 2);
                    client.share(args[1]);
                    break loop;
                
                case LiftOpt.FILES:
                    client.files();
                    break loop;
                
                case LiftOpt.RM:
                    checkOptArgsNumber(LiftOpt.RM, args, 2);
                    client.rm(args[1]);
                    break loop;
                    
                case LiftOpt.ADD:
                    checkOptArgsNumber(LiftOpt.ADD, args, 2);
                    client.add(args[1]);
                    break loop;
                
                case LiftOpt.ID:
                    client.id();
                    break loop;
                
                case LiftOpt.GET:
                    checkOptArgsNumber(LiftOpt.GET, args, 2);
                    client.get(args[1]);
                    break loop;
                    
                case LiftOpt.UFL:
                    checkOptArgsNumber(LiftOpt.UFL, args, 2);
                    client.ufl(args[1]);
                    break loop;
                
                case LiftOpt.VERSION:
                    client.version();
                    break loop;
                
                case LiftOpt.HELP:
                    showUsage();
                    break loop;
                
                default:
                    System.out.println("lift: '" + opt + "' is not a lift command.");
                    System.out.println("See 'lift --help'");
                    break loop;
            
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
    
    private static void checkOptArgsNumber(String opt, String[] args, int requiredArgsNumber) {
        if (args.length != requiredArgsNumber) {
            System.out.println("'lift " + opt + "' requires exactly " + (requiredArgsNumber - 1) + " argument(s)");
            System.out.println("See 'lift --help'");
            System.exit(0);
        }
    }
}
