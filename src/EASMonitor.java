
import common.Component;
import event.Event;
import event.RabbitMQInterface;
import instrumentation.Indicator;
import instrumentation.MessageWindow;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author tania
 */
public class EASMonitor extends Thread {

    private RabbitMQInterface em = null;            // Interface object to the event manager
    private String evtMgrIP = null;			// Event Manager IP address			// and humidity is in relative humidity percentage.
    boolean registered = true;				// Signifies that this class is registered with an event manager.
    MessageWindow messageWin = null;			// This is the message window
    Indicator doorIndicator;				
    Indicator windowIndicator;				
    Indicator intruderIndicator;

    public EASMonitor() {
        // event manager is on the local system
        try {
            // Here we create an event manager interface object. This assumes
            // that the event manager is on the local machine
            em = new RabbitMQInterface();
        }
        catch (Exception e) {
            System.out.println("EASMonitor::Error instantiating event manager interface: " + e);
            registered = false;
        } // catch // catch
    } //Constructor

    public EASMonitor(String evmIpAddress) {
        // event manager is not on the local system
        evtMgrIP = evmIpAddress;
        try {
            // Here we create an event manager interface object. This assumes
            // that the event manager is NOT on the local machine
            em = new RabbitMQInterface();
            Component.SERVER_IP = evtMgrIP;
        }
        catch (Exception e) {
            System.out.println("EASMonitor::Error instantiating event manager interface: " + e);
            registered = false;
        } // catch // catch
    } // Constructor

    @Override
    public void run() {
        Event evt = null;			// Event object
        int evtId = 0;				// User specified event ID
        String currentDoorStatus = "0";
        String currentWindowStatus = "0";
        String currentMotionStatus = "0";
        int delay = 1000;			// The loop delay (1 second)
        boolean isDone = false;			// Loop termination flag
        boolean on = true;			// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
        boolean off = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers
        
        if (em != null) {
            // Now we create the ECS status and message panel
            // Note that we set up two indicators that are initially yellow. This is
            // because we do not know if the temperature/humidity is high/low.
            // This panel is placed in the upper left hand corner and the status 
            // indicators are placed directly to the right, one on top of the other

            messageWin = new MessageWindow("EAS Monitoring Console", 0, 1);
            windowIndicator = new Indicator("WINDOW CLOSED", messageWin.getX() + messageWin.width(), 0, 1);
            doorIndicator = new Indicator("DOOR CLOSED", messageWin.getX() + messageWin.width(), (int) (messageWin.height() / 2), 1);
            intruderIndicator = new Indicator("MOTION NONE", messageWin.getX() + messageWin.width(), messageWin.height(), 1);

            messageWin.writeMessage("Registered with the event manager.");

            /**
             * ******************************************************************
             ** Here we start the main simulation loop
             * *******************************************************************
             */
            while (!isDone) {
                // Here we get our event queue from the event manager
                try {
                    em.suscribeMsg("queueSensors", "sensors");
                } // try
                catch (Exception e) {
                    messageWin.writeMessage("Error getting event queue::" + e);
                } // catch // catch

                // If there are messages in the queue, we read through them.
                // We are looking for EventIDs = 1 or 2. Event IDs of 1 are temperature
                // readings from the temperature sensor; event IDs of 2 are humidity sensor
                // readings. Note that we get all the messages at once... there is a 1
                // second delay between samples,.. so the assumption is that there should
                // only be a message at most. If there are more, it is the last message
                // that will effect the status of the temperature and humidity controllers
                // as it would in reality.
                int msg_numero = em.getEventId();
                String msg_texto = em.getMessage();

                if (msg_numero == common.Component.DOOR) { 
                    try {
                        currentDoorStatus = msg_texto;
                        if("1.0".equals(currentDoorStatus)){
                            doorIndicator.setLampColorAndMessage("DOOR OPENED!!!!", 3);
                        }
                   } // try
                   catch (Exception e) {
                        messageWin.writeMessage("Error reading door status: " + msg_texto);
                   } // catch // catch
                } // if

               if (msg_numero == common.Component.WINDOW) { // Humidity reading
                    try {
                        currentWindowStatus = msg_texto;
                        if("1.0".equals(currentWindowStatus)){
                            windowIndicator.setLampColorAndMessage("WINDOW OPENED!!!!", 3);
                        }
                    } // try
                        catch (Exception e) {
                        messageWin.writeMessage("Error reading window status: " + e);
                    } // catch // catch
                } // if
               
                if (msg_numero == common.Component.MOTION) { // Humidity reading
                    try {
                        currentMotionStatus = msg_texto;
                        if("1.0".equals(currentMotionStatus)){
                            intruderIndicator.setLampColorAndMessage("MOTION DETECTED!!!!", 3);
                        }
                    } // try
                        catch (Exception e) {
                        messageWin.writeMessage("Error reading motion sensor status: " + e);
                    } // catch // catch
                } // if

                    // If the event ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the event manager.
                if (msg_numero == 99) {
                    isDone = true;
                        

                    messageWin.writeMessage("\n\nSimulation Stopped. \n");
                        // Get rid of the indicators. The message panel is left for the
                        // user to exit so they can see the last message posted.
                    doorIndicator.dispose();
                    windowIndicator.dispose();
                    intruderIndicator.dispose();
                } // if
                
                messageWin.writeMessage("Door:: " + currentDoorStatus + "  Window:: " + currentWindowStatus+" Motion detected:: "+currentMotionStatus);

                // This delay slows down the sample rate to Delay milliseconds
                try {
                    Thread.sleep(delay);
                } // try
                catch (Exception e) {
                    System.out.println("Sleep error:: " + e);
                } // catch
            } // while
        }
        else {
            System.out.println("Unable to register with the event manager.\n\n");
        } // if
    } // main

    /**
     * This method returns the registered status
     *
     * @return boolean true if registered, false if not registered
     */
    public boolean isRegistered() {
        return (registered);
    } // setTemperatureRange


    /**
     * This method posts an event that stops the environmental control system.
     * Exceptions: Posting to event manager exception
     */
    public void startSensors() {
        messageWin.writeMessage("***START MESSAGE RECEIVED - INITIALIZING SENSORS***");
        String message = "";
        Event evt;
        
        evt = new Event(Component.START);
        message = evt.Event1(Component.START,String.valueOf(Component.START));
        doorIndicator.setLampColorAndMessage("DOOR CLOSED", 1);
        windowIndicator.setLampColorAndMessage("WINDOW CLOSED", 1);
        intruderIndicator.setLampColorAndMessage("MOTION NONE", 1);
        try {
            em.publishMsg(message, "monitor");
        } // try
        catch (Exception e) {
            System.out.println("Error sending monitor control message:: " + e);
        }
    } // star sensors
    
    public void stopSensors() {
        messageWin.writeMessage("***STOP MESSAGE RECEIVED - STOPPING SENSORS***");
        String message = "";
        Event evt;
        
        evt = new Event(Component.STOP);
        message = evt.Event1(Component.STOP,String.valueOf(Component.STOP));
        
        try {
            em.publishMsg(message, "monitor");
        } // try
        catch (Exception e) {
            System.out.println("Error sending monitor control message:: " + e);
        }
    } // stop sensors

   
} // ECSMonitor