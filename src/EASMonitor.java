
import common.Component;
import event.Event;
import event.RabbitMQInterface;
import instrumentation.Indicator;
import instrumentation.MessageWindow;
import java.util.Timer;
import java.util.TimerTask;

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
    Indicator fireIndicator;
    int fuego = 0;
    Timer t = new Timer();
    TimerTask tt = new TimerTask() {

        @Override
        public void run() {
            if(fuego == 0) {
                startSprinklers();
                fuego = 1;
            } else {
                t.cancel();
                t.purge();
            }
        }
    ;

    };

    public EASMonitor() {
        // event manager is on the local system
        try {
            // Here we create an event manager interface object. This assumes
            // that the event manager is on the local machine
            em = new RabbitMQInterface();
        } catch (Exception e) {
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
        } catch (Exception e) {
            System.out.println("EASMonitor::Error instantiating event manager interface: " + e);
            registered = false;
        } // catch // catch
    } // Constructor

    @Override
    public void run() {
        Event evt = null;			// Event object
        int evtId = 0;

        String currentDoorStatus = "0";
        String currentWindowStatus = "0";
        String currentMotionStatus = "0";
        String currentFireStatus = "0";
        int delay = 1000;			// The loop delay (1 second)
        int delaySp = 1500;
        boolean isDone = false;			// Loop termination flag

        if (em != null) {
            // Now we create the ECS status and message panel
            // Note that we set up two indicators that are initially yellow. This is
            // because we do not know if the temperature/humidity is high/low.
            // This panel is placed in the upper left hand corner and the status 
            // indicators are placed directly to the right, one on top of the other

            messageWin = new MessageWindow("EAS Monitoring Console", 0, 0);
            windowIndicator = new Indicator("WINDOW CLOSED", messageWin.getX() + messageWin.width(), 0, 1);
            doorIndicator = new Indicator("DOOR CLOSED", messageWin.getX() + messageWin.width(), (int) (messageWin.height() / 2), 1);
            intruderIndicator = new Indicator("MOTION NONE", messageWin.getX() + messageWin.width(), messageWin.height(), 1);
            fireIndicator = new Indicator("FIRE UNK", messageWin.getX() + messageWin.width(), (int) (messageWin.height() * 1.5), 1);

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

                int msg_numero = em.getEventId();
                String msg_texto = em.getMessage();

                if (msg_numero == common.Component.DOOR) {
                    try {
                        currentDoorStatus = msg_texto;
                        if ("1.0".equals(currentDoorStatus)) {
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
                        if ("1.0".equals(currentMotionStatus)) {
                            windowIndicator.setLampColorAndMessage("WINDOW OPENED!!!!", 3);
                        }
                    } // try
                    catch (Exception e) {
                        messageWin.writeMessage("Error reading window status: " + e);
                    } // catch // catch
                } // if

                if (msg_numero == common.Component.MOTION) {
                    try {
                        currentMotionStatus = msg_texto;
                        if ("1.0".equals(currentMotionStatus)) {
                            windowIndicator.setLampColorAndMessage("MOTION DETECTED!!!!", 3);
                        }
                    } // try
                    catch (Exception e) {
                        messageWin.writeMessage("Error reading motion sensor status: " + e);
                    } // catch // catch
                } // if

                if (msg_numero == common.Component.FIRE) { // Humidity reading
                    try {
                        currentFireStatus = msg_texto;

                        if ("1.0".equals(currentFireStatus)) {

                            fireIndicator.setLampColorAndMessage("FIRE!!!!", 3);
                            messageWin.writeMessage("FUEGO PRENDER ROCIADORES");
                            if(fuego == 0){
                                t.schedule(tt, delaySp);
      
                            }
                        }
                    } // try
                    catch (Exception e) {
                        messageWin.writeMessage("Error reading fire sensor status: " + e);
                    } // catch // catch
                } // if

                // If the event ID == 99 then this is a signal that the simulation
                // is to end. At this point, the loop termination flag is set to
                // true and this process unregisters from the event manager.
                if (msg_numero == 99) {
                    isDone = true;

                    messageWin.writeMessage("\n\nSimulation Stopped. \n");

                    doorIndicator.dispose();
                    windowIndicator.dispose();
                    intruderIndicator.dispose();
                } // if

                messageWin.writeMessage("Door:: " + currentDoorStatus + "  Window:: " + currentWindowStatus + " Motion detected:: " + currentMotionStatus);
                messageWin.writeMessage("Fire:: " + currentFireStatus);

                // This delay slows down the sample rate to Delay milliseconds
                try {
                    Thread.sleep(delay);
                } // try
                catch (Exception e) {
                    System.out.println("Sleep error:: " + e);
                } // catch
            } // while
        } else {
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
        message = evt.Event1(Component.START, String.valueOf(Component.START));
        doorIndicator.setLampColorAndMessage("DOOR CLOSED", 1);
        windowIndicator.setLampColorAndMessage("WINDOW CLOSED", 1);
        intruderIndicator.setLampColorAndMessage("MOTION NONE", 1);
        fireIndicator.setLampColorAndMessage("MUSEUM OK", 1);
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
        message = evt.Event1(Component.STOP, String.valueOf(Component.STOP));

        try {
            em.publishMsg(message, "monitor");
        } // try
        catch (Exception e) {
            System.out.println("Error sending monitor control message:: " + e);
        }
    } // stop sensors

    public void startSprinklers() {
        messageWin.writeMessage("***START MESSAGE RECEIVED - STARTING SPRINKLERS***");
        String message = "";
        Event evt;
        fuego = 0;
        t.cancel();
        t.purge();
        evt = new Event(Component.STARTSPRINKLER);
        message = evt.Event1(Component.STARTSPRINKLER, String.valueOf(Component.STARTSPRINKLER));

        try {
            em.publishMsg(message, "monitor");
        } // try
        catch (Exception e) {
            System.out.println("Error sending monitor control message:: " + e);
        }
    } // stop sensors

    public void stopSprinklers() {
        messageWin.writeMessage("***STOP MESSAGE RECEIVED - STOPPING SPRINKLERS***");
        String message = "";
        Event evt;

        evt = new Event(Component.STOPSPRINKLER);
        message = evt.Event1(Component.STOPSPRINKLER, String.valueOf(Component.STOPSPRINKLER));

        try {
            em.publishMsg(message, "monitor");
        } // try
        catch (Exception e) {
            System.out.println("Error sending monitor control message:: " + e);
        }
    } // stop sensors
} // ECSMonitor
