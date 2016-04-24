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
public class BrokenWindow extends Sensor implements Runnable {

    private int doorState = 0;	// Humidifier state: false == off, true == on
    private boolean sensorState = true;
   
    private static BrokenWindow INSTANCE = new BrokenWindow();
    
    private BrokenWindow(){
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

            MessageWindow messageWin = new MessageWindow("Door Sensor", winPosX, winPosY);
            messageWin.writeMessage("Registered with the event manager.");

            try {
                //messageWin.writeMessage("   Participant id: " + evtMgrI.getMyId());
                //messageWin.writeMessage("   Registration Time: " + evtMgrI.getRegistrationTime());
            } 
            catch (Exception e) {
                messageWin.writeMessage("Error:: " + e);
            } 

            messageWin.writeMessage("\nMostrando estatus de la puerta::");
            
            
            /**
             * ******************************************************************
             ** Here we start the main simulation loop
             * *******************************************************************
             */
            messageWin.writeMessage("Beginning Simulation... ");
            while (!isDone) {
                // Post the current relative humidity
                postEvent(evtMgrI, DOOR, doorState);
                messageWin.writeMessage("Current Door Status:: " + doorState + "%");
                // Get the message queue
                try {
                    String message = evtMgrI.returnMessage();//returnMessage de rabbitmq
                } 
                catch (Exception e) {
                    messageWin.writeMessage("Error getting event queue::" + e);
                } 

                // If there are messages in the queue, we read through them.
                // We are looking for EventIDs = -4, this means the the humidify or
                // dehumidifier has been turned on/off. Note that we get all the messages
                // from the queue at once... there is a 2.5 second delay between samples,..
                // so the assumption is that there should only be a message at most.
                // If there are more, it is the last message that will effect the
                // output of the humidity as it would in reality.
                
                try {
                    int qlen = queue.getSize();

                for (int i = 0; i < qlen; i++) {
                    evt = queue.getEvent();
                    if (evt.getEventId() == DOOR_SENSOR) {
                        if (evt.getMessage().equalsIgnoreCase(ALARMS_ON)) // humidifier on
                        {
                            sensorState = true;
                        } 

                        if (evt.getMessage().equalsIgnoreCase(ALARMS_OFF)) // humidifier off
                        {
                            sensorState = false;
                        } 

                       
                    }

                    // If the event ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the event manager.
                    if (evt.getEventId() == END) {
                        isDone = true;

                        try {
                            //evtMgrI.unRegister();
                        }
                        catch (Exception e) {
                            messageWin.writeMessage("Error unregistering: " + e);
                        } 
                        messageWin.writeMessage("\n\nSimulation Stopped. \n");
                    } 
                } 
                } 
                catch (Exception e) {}
                if (sensorState) {
                    float semilla = getRandomNumber();
                    if(semilla>0.1){
                        doorState = 0;
                    }else{
                        doorState = 1;
                    }
                } // if humidifier is on
                // Now we trend the relative humidity according to the status of the
                // humidifier/dehumidifier controller.
                if (doorState == 1) {
                     messageWin.writeMessage("Puerta abierta, suena alarma");
                } // if humidifier is on

                // Here we wait for a 2.5 seconds before we start the next sample
                try {
                    Thread.sleep(delay);
                }
                catch (Exception e) {
                    messageWin.writeMessage("Sleep error:: " + e);
                } 
            } 
        }
        else {
            System.out.println("Unable to register with the event manager.\n\n");
        } 
    }
    
    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized (BrokenDoor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BrokenWindow();
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
    public static BrokenWindow getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    /**
     * Start this sensor
     * 
     * @paramargs IP address of the event manager (on command line). 
     * If blank, it is assumed thast the event manager is on the local machine.
     */
    public static void main(String args[]) {
        Component.SERVER_IP = "localhost";
        BrokenWindow sensor = BrokenWindow.getInstance();
        sensor.run();
    }

} 