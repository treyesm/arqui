/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import common.Component;
import static common.Component.DEHUMIDIFIER_OFF;
import static common.Component.DEHUMIDIFIER_ON;
import static common.Component.END;
import instrumentation.Indicator;
import instrumentation.MessageWindow;

/**
 *
 * @author tania
 */
public class AlarmsController extends Controller implements Runnable {

    Indicator doorIndicator;				
    Indicator windowIndicator;				
    Indicator intruderIndicator;
    Indicator fireIndicator;
    int doorState = 0;
    int windowState = 0;
    int motionState = 0;
    int fireState = 0;
    
    private static AlarmsController INSTANCE = new AlarmsController();

    private AlarmsController() {
    }

    @Override
    public void run() {
        // Here we check to see if registration worked. If em is null then the
        // event manager interface was not properly created.
        
            System.out.println("Registered with the event manager.");

            float winPosX = 0.0f; 	//This is the X position of the message window in terms 
            //of a percentage of the screen height
            float winPosY = 0.60f;	//This is the Y position of the message window in terms 
            //of a percentage of the screen height 

            MessageWindow messageWin = new MessageWindow("Security Controller Status Console", winPosX, winPosY);
    
            messageWin.writeMessage("Registered with the event manager.");

            try {
                messageWin.writeMessage("   Participant id: " + evtMgrI.getMyId());
                messageWin.writeMessage("   Registration Time: " + evtMgrI.getRegistrationTime());
            }
            catch (Exception e) {
                System.out.println("Error:: " + e);
            }

            /**
             * ******************************************************************
             ** Here we start the main simulation loop
             * *******************************************************************
             */
            while (!isDone) {
                try {
                    evtMgrI.returnMessage();  //returnMessage de rabbitmq
                }
                catch (Exception e) {
                    messageWin.writeMessage("Error getting event queue::" + e);
                }

                if (evtMgrI.getEventId() == FIRE_CONTROLLER) {
                    if (evtMgrI.getMessage().equalsIgnoreCase(HUMIDIFIER_ON)) { // humidifier on
                        fireState = 1;
                        messageWin.writeMessage("Received humidifier on event");

                        // Confirm that the message was recieved and acted on
                        confirmMessage(evtMgrI, HUMIDITY_SENSOR, HUMIDIFIER_ON);
                    }
                    if (evtMgrI.getMessage().equalsIgnoreCase(HUMIDIFIER_OFF)) { // humidifier off
                        fireState = 0;
                        messageWin.writeMessage("Received humidifier off event");

                        // Confirm that the message was recieved and acted on
                        confirmMessage(evtMgrI, HUMIDITY_SENSOR, HUMIDIFIER_OFF);
                    }
                    
                }

                // If the event ID == 99 then this is a signal that the simulation
                // is to end. At this point, the loop termination flag is set to
                // true and this process unregisters from the event manager.
                
                if (evtMgrI.getEventId() == END) {
                    isDone = true;
                    
                    messageWin.writeMessage("\n\nSimulation Stopped. \n");

                }


                
                try {
                    Thread.sleep(delay);
                }
                catch (Exception e) {
                    System.out.println("Sleep error:: " + e);
                }
            }
        
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized (AlarmsController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmsController();
                }
            }
        }
    }

    /**
     * This method calls createInstance method to creates and ensure that 
     * only one instance of this class is created. Singleton design pattern.
     * 
     * @return The instance of this class.
     */
    public static AlarmsController getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    /**
     * Start this controller
     * 
     * @param args IP address of the event manager (on command line). 
     * If blank, it is assumed that the event manager is on the local machine.
     */
    public static void main(String args[]) {
        Component.SERVER_IP = "localhost";
        AlarmsController sensor = AlarmsController.getInstance();
        sensor.run();
    }
}

