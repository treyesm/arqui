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
                System.out.println("W: Start sprinklers\n");
                System.out.println("O: Stop sprinklers\n");
                System.out.print("\n>>>> ");
                option = userInput.keyboardReadString();

                //////////// option X ////////////
                if (option.equalsIgnoreCase("X")) {

                    System.out.println("\nAlarm sensor stopped... Press S to start again");
                    monitor.stopSensors();
                    
                } // if
                //////////// option S ////////////
                if (option.equalsIgnoreCase("S")) {

                    System.out.println("\nAlarm sensor started... Press X to stop them again.");
                    monitor.startSensors();
                    
                } // if
                if (option.equalsIgnoreCase("W")) {

                    System.out.println("\nSprinklers started... Press X to stop them again.");
                    monitor.startSprinklers();
                    
                } // if
                if (option.equalsIgnoreCase("O")) {

                    System.out.println("\nSprinklers stopped... Press X to stop them again.");
                    monitor.stopSprinklers();
                    
                } // if
            } // while
        }
        else {
            System.out.println("\n\nUnable start the monitor.\n\n");
        } // if
    } // main
} // ECSConsole
