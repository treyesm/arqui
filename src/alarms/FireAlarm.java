/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alarms;

import common.Component;
import instrumentation.MessageWindow;
import sensors.Sensor;

/**
 *
 * @author tania
 */
public class FireAlarm extends Sensor implements Runnable {

    private int fireState = 0;	// door state: 0 == close, 1 == open
    private boolean sensorState = true;

    private static FireAlarm INSTANCE = new FireAlarm();

    private FireAlarm() {
    }

    @Override
    public void run() {
        // Here we check to see if registration worked. If ef is null then the
        // event manager interface was not properly created.
        if (evtMgrI != null) {

            // We create a message window. Note that we place this panel about 1/2 across 
            // and 2/3s down the screen
            float winPosX = 0.5f; 	//This is the X position of the message window in terms 
            //of a percentage of the screen height
            float winPosY = 0.60f;	//This is the Y position of the message window in terms 
            //of a percentage of the screen height 

            MessageWindow messageWin = new MessageWindow("Fire Sensor", winPosX, winPosY);
            messageWin.writeMessage("Registered with the event manager.");

            try {
                messageWin.writeMessage("   Participant id: " + evtMgrI.getMyId());
                messageWin.writeMessage("   Registration Time: " + evtMgrI.getRegistrationTime());
            } catch (Exception e) {
                messageWin.writeMessage("Error:: " + e);
            }

            messageWin.writeMessage("\nFire status::");

            /**
             * ******************************************************************
             ** Here we start the main simulation loop
             * *******************************************************************
             */
            messageWin.writeMessage("Beginning Simulation... ");
            while (!isDone) {

                // Get the message queue
                try {
                    evtMgrI.returnMessage();  //returnMessage de rabbitmq
                } catch (Exception e) {
                    messageWin.writeMessage("Error getting event queue::" + e);
                }

                try {

                    // If the event ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the event manager.
                    if (evtMgrI.getEventId() == END) {
                        isDone = true;

                        messageWin.writeMessage("\n\nSimulation Stopped. \n");
                    }

                } catch (Exception e) {
                }

                try {
                    Thread.sleep(delay);

                    if (sensorState == true) {
                        if (fireState == 0) {
                            messageWin.writeMessage("Current Museum Status:: ok");
                        } else {
                            messageWin.writeMessage("Current Museum Status:: FIRE DETECTED!!!");
                        }
                        float semilla = getRandomNumber();

                        if (semilla > 0.5) {
                            fireState = 0;
                        } else {
                            fireState = 1;
                        }
                        if (fireState == 1) {
                            messageWin.writeMessage("Fire, send alarm");
                        }

                        postEvent(evtMgrI, FIRE, fireState);
                    }
                } catch (Exception e) {
                    messageWin.writeMessage("Sleep error:: " + e);
                }

            }
        } else {
            System.out.println("Unable to register with the event manager.\n\n");
        }
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized (FireAlarm.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FireAlarm();
                }
            }
        }
    }

    /**
     * This method calls createInstance method to creates and ensure that only
     * one instance of this class is created. Singleton design pattern.
     *
     * @return The instance of this class.
     */
    public static FireAlarm getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    /**
     * Start this sensor
     *
     * @paramargs IP address of the event manager (on command line). If blank,
     * it is assumed thast the event manager is on the local machine.
     */
    public static void main(String args[]) {
        Component.SERVER_IP = "localhost";
        FireAlarm sensor = FireAlarm.getInstance();
        sensor.run();
    }

}

