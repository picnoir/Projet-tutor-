/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.ProfileException;

import jade.util.leap.Properties;
import jade.util.ExtendedProperties;
import jade.util.Logger;

/**
 *
 * @author josuah
 * 
 * The launching code would look like the following:
 * 
 * String [] args = {"-cp", "lib/jade.jar:Jade/build", "-container"};
 * 
 * jade.wrapper.AgentContainer container = Launcher.boot(args);
 * 
 * String [] agentArgs = new String [0];
 * AgentController agent = container.createNewAgent(new AID("client", AID.ISLOCALNAME),"agent.Client", args);
 * agent.start(); // not sure about this one
 * 
 */
public class Launcher
{

    public static final String DEFAULT_FILENAME = "leap.properties";
    private static final Logger logger = Logger.getMyLogger("jade.Boot");

    /**
     * Fires up the <b><em>JADE</em></b> system. This method initializes the
     * Profile Manager and then starts the bootstrap process for the
     * <B><em>JADE</em></b>
     * agent platform.
     * @param args
     * @return 
     */
    public static jade.wrapper.AgentContainer boot(String args[])
    {
        jade.wrapper.AgentContainer container = null;
        
        try {
            // Create the Profile 
            ProfileImpl p = null;
            if (args.length > 0) 
            {
                if (args[0].startsWith("-"))
                {
                    // Settings specified as command line arguments
                    Properties pp = parseCmdLineArgs(args);
                    if (pp != null)
                    {
                        p = new ProfileImpl(pp);
                    } 
                    else
                    {
                        // One of the "exit-immediately" options was specified!
                        return null;
                    }
                }
                else
                {
                    // Settings specified in a property file
                    p = new ProfileImpl(args[0]);
                }
            }
            else
            {
                // Settings specified in the default property file
                p = new ProfileImpl(DEFAULT_FILENAME);
            }

            // Start a new JADE runtime system
            Runtime.instance().setCloseVM(true);
			//#PJAVA_EXCLUDE_BEGIN
            // Check whether this is the Main Container or a peripheral container
            if (p.getBooleanProperty(Profile.MAIN, true)) 
            {
                 container = Runtime.instance().createMainContainer(p);
            }
            else
            {
                container =  Runtime.instance().createAgentContainer(p);
            }
            
			//#PJAVA_EXCLUDE_END
			/*#PJAVA_INCLUDE_BEGIN
             // Starts the container in SINGLE_MODE (Only one per JVM)
             Runtime.instance().startUp(p);
             #PJAVA_INCLUDE_END*/
        } 
        catch (ProfileException pe)
        {
            System.err.println("Error creating the Profile [" + pe.getMessage() + "]");
            pe.printStackTrace();
            printUsage();
            //System.err.println("Usage: java jade.Launcher <filename>");
            System.exit(-1);
        }
        catch (IllegalArgumentException iae)
        {
            System.err.println("Command line arguments format error. " + iae.getMessage());
            iae.printStackTrace();
            printUsage();
            //System.err.println("Usage: java jade.Launcher <filename>");
            System.exit(-1);
        }
        
        return container;
    }

    public static Properties parseCmdLineArgs(String[] args) throws IllegalArgumentException
    {
        Properties props = new ExtendedProperties();

        int i = 0;
        while (i < args.length) {
            if (args[i].startsWith("-"))
            {
				// Parse next option

                // Switch options require special handling
                if (args[i].equalsIgnoreCase("-version"))
                {
                    logger.log(Logger.INFO, "----------------------------------\n" + Runtime.getCopyrightNotice() + "----------------------------------------");
                    return null;
                }
                
                if (args[i].equalsIgnoreCase("-help"))
                {
                    printUsage();
                    return null;
                }
                
                if (args[i].equalsIgnoreCase("-container"))
                {
                    props.setProperty(Profile.MAIN, "false");
                }
                else if (args[i].equalsIgnoreCase("-" + Profile.LOCAL_SERVICE_MANAGER))
                {
                    props.setProperty(Profile.LOCAL_SERVICE_MANAGER, "true");
                }
                else if (args[i].equalsIgnoreCase("-" + Profile.GUI))
                {
                    props.setProperty(Profile.GUI, "true");
                }
                else if (args[i].equalsIgnoreCase("-" + Profile.NO_MTP))
                {
                    props.setProperty(Profile.NO_MTP, "true");
                } // Options that can be specified in different ways require special handling
                else if (args[i].equalsIgnoreCase("-name"))
                {
                    if (++i < args.length)
                    {
                        props.setProperty(Profile.PLATFORM_ID, args[i]);
                    }
                    else
                    {
                        throw new IllegalArgumentException("No platform name specified after \"-name\" option");
                    }
                } 
                else if (args[i].equalsIgnoreCase("-mtp"))
                {
                    if (++i < args.length)
                    {
                        props.setProperty(Profile.MTPS, args[i]);
                    }
                    else
                    {
                        throw new IllegalArgumentException("No mtps specified after \"-mtp\" option");
                    }
                } // The -conf option requires special handling 
                else if (args[i].equalsIgnoreCase("-conf"))
                {
                    if (++i < args.length)
                    {
                        // Some parameters are specified in a properties file
                        try
                        {
                            props.load(args[i]);
                        }
                        catch (Exception e)
                        {
                            if (logger.isLoggable(Logger.SEVERE))
                            {
                                logger.log(Logger.SEVERE, "WARNING: error loading properties from file " + args[i] + ". " + e);
                            }
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException("No configuration file name specified after \"-conf\" option");
                    }
                } // Default handling for all other properties
                else
                {
                    String name = args[i].substring(1);
                    if (++i < args.length)
                    {
                        props.setProperty(name, args[i]);
                    }
                    else
                    {
                        throw new IllegalArgumentException("No value specified for property \"" + name + "\"");
                    }
                }
                ++i;
            }
            else
            {
                // Get agents at the end of command line
                if (props.getProperty(Profile.AGENTS) != null)
                {
                    if (logger.isLoggable(Logger.WARNING))
                    {
                        logger.log(Logger.WARNING, "WARNING: overriding agents specification set with the \"-agents\" option");
                    }
                }
                String agents = args[i];
                props.setProperty(Profile.AGENTS, args[i]);
                if (++i < args.length)
                {
                    if (logger.isLoggable(Logger.WARNING))
                    {
                        logger.log(Logger.WARNING, "WARNING: ignoring command line argument " + args[i] + " occurring after agents specification");
                    }
                    if (agents != null && agents.indexOf('(') != -1 && !agents.endsWith(")"))
                    {
                        if (logger.isLoggable(Logger.WARNING))
                        {
                            logger.log(Logger.WARNING, "Note that agent arguments specifications must not contain spaces");
                        }
                    }
                    if (args[i].indexOf(':') != -1)
                    {
                        if (logger.isLoggable(Logger.WARNING))
                        {
                            logger.log(Logger.WARNING, "Note that agent specifications must be separated by a semicolon character \";\" without spaces");
                        }
                    }
                }
                break;
            }
        }

        // Consistency check
        if ("true".equals(props.getProperty(Profile.NO_MTP)) && props.getProperty(Profile.MTPS) != null)
        {
            if (logger.isLoggable(Logger.WARNING))
            {
                logger.log(Logger.WARNING, "WARNING: both \"-mtps\" and \"-nomtp\" options specified. The latter will be ignored");
            }
            props.remove(Profile.NO_MTP);
        }

        return props;
    }

    public static void printUsage()
    {
        System.out.println("Usage:");
        System.out.println("java -cp <classpath> jade.Boot [options] [agents]");
        System.out.println("Main options:");
        System.out.println("    -container");
        System.out.println("    -gui");
        System.out.println("    -name <platform name>");
        System.out.println("    -host <main container host>");
        System.out.println("    -port <main container port>");
        System.out.println("    -local-host <host where to bind the local server socket on>");
        System.out.println("    -local-port <port where to bind the local server socket on>");
        System.out.println("    -conf <property file to load configuration properties from>");
        System.out.println("    -services <semicolon separated list of service classes>");
        System.out.println("    -mtps <semicolon separated list of mtp-specifiers>");
        System.out.println("     where mtp-specifier = [in-address:]<mtp-class>[(comma-separated args)]");
        System.out.println("    -<property-name> <property-value>");
        System.out.println("Agents: [-agents] <semicolon separated list of agent-specifiers>");
        System.out.println("     where agent-specifier = <agent-name>:<agent-class>[(comma separated args)]");
        System.out.println();
        System.out.println("Look at the JADE Administrator's Guide for more details");
    }
}
