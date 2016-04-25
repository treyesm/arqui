/**
 * **************************************************************************************
 * File:ECSConsole.java 
 * Course: Software Architecture 
 * Project: Event Architectures
 * Institution: Autonomous University of Zacatecas 
 * Date: November 2015
 * Developer: Ferman Ivan Tovar 
 * Reviewer: Perla Velasco Elizondo
 * **************************************************************************************
 * This class is the console for the museum environmental control system. This
 * process consists of two threads. The ECSMonitor object is a thread that is
 * started that is responsible for the monitoring and control of the museum
 * environmental systems. The main thread provides a text interface for the user
 * to change the temperature and humidity ranges, as well as shut down the
 * system.
 * **************************************************************************************
 */

import common.*;

public class EASConsole {

    public static void main(String args[]) {
        IOManager userInput = new IOManager();	// IOManager IO Object
        boolean isDone = false;			// Main loop flag
        String option;                          // Menu choice from user
        boolean isError;                        // Error flag
        EASMonitor monitor;                     // The environmental control system monitor
       
        /////////////////////////////////////////////////////////////////////////////////
        // Get the IP address of the event manager
        /////////////////////////////////////////////////////////////////////////////////
        if (args.length != 0) {
            // event manager is not on the local system
            monitor = new EASMonitor(args[0]);
        }
        else {
            monitor = new EASMonitor();
        } // if

        // Here we check to see if registration worked. If ef is null then the
        // event manager interface was not properly created.
        if (monitor.isRegistered()) {
            monitor.start(); // Here we start the monitoring and control thread

            while (!isDone) {
                // Here, the main thread continues and provides the main menu
                System.out.println("\n\n\n\n");
                System.out.println("Environmental Alarm System (EAS) Command Console: \n");

                if (args.length != 0) {
                    System.out.println("Using event manger at: " + args[0] + "\n");
                }
                else {
                    System.out.println("Using local event manger \n");
                }

                System.out.println("Select an Option: \n");
                System.out.println("X: Stop Alarm sensors\n");
                System.out.println("S: Start Alarm sensors\n");
                System.out.print("\n>>>> ");
                option = userInput.keyboardReadString();

                //////////// option X ////////////
                if (option.equalsIgnoreCase("X")) {
		// Here the user is done, so we set the Done flag and halt
                    // the environmental control system. The monitor provides a method
                    // to do this. Its important to have processes release their queues
                    // with the event manager. If these queues are not released these
                    // become dead queues and they collect events and will eventually
                    // cause problems for the event manager.

                    isDone = true;
                    System.out.println("\nConsole Stopped... Exit monitor mindow to return to command prompt.");
                    monitor.stopSensors();
                    option = userInput.keyboardReadString();
                } // if
                //////////// option X ////////////
                if (option.equalsIgnoreCase("S")) {
                    monitor.startSensors();
                    isDone = true;
                    System.out.println("\nConsole Stopped... Exit monitor mindow to return to command prompt.");
                    monitor.startSensors();
                    option = userInput.keyboardReadString();
                } // if
            } // while
        }
        else {
            System.out.println("\n\nUnable start the monitor.\n\n");
        } // if
    } // main
} // ECSConsole
