/**
 * **************************************************************************************
 * File:EventManagerInterface.java
 * Course: Software Architecture
 * Project: Event Architectures
 * Institution: Autonomous University of Zacatecas
 * Date: November 2015
 * Developer: Equipo de Zelma
 * Reviewer: Perla Velasco Elizondo
 * **************************************************************************************
 * This class provides an interface to the event manager for
 * participants (processes), enabling them to to send and receive events between
 * participants. A participant is any thing (thread, object, process) that
 * instantiates an EventEventManagerInterface object - this automatically
 * attempts to register that entity with the event manager
 * **************************************************************************************
 */
package event;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Equipo de Zelma
 */
public class RabbitMQInterface {

    private static final String sending = "fanout";
    Connection connection;  
    Channel channel;
    private long participantId = -1;			// This processes ID
    private static String message = "";
    Calendar TimeStamp = Calendar.getInstance();
    SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
    
    public RabbitMQInterface() {

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        try {

            try {

                connection = factory.newConnection();
                //channel = connection.createChannel();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        
      
    }
    
    class ParticipantNotRegisteredException extends Exception {

        ParticipantNotRegisteredException() {
            super();
        }

        ParticipantNotRegisteredException(String s) {
            super(s);
        }

    } // Exception

    
    
    /**
     * This method allows participants to get their participant Id.
     *
     * @return The ID
     * @throws event.RabbitMQInterface.ParticipantNotRegisteredException
     */
    public long getMyId() throws RabbitMQInterface.ParticipantNotRegisteredException {
        
        if (participantId != -1) {
            return participantId;
        } else {
            throw new RabbitMQInterface.ParticipantNotRegisteredException("Participant not registered");
        } // if
        
    } // getMyId
    
    /**
     * This method allows participants to obtain the time of registration.
     *
     * @return String time stamp in the format: yyyy MM dd::hh:mm:ss:SSS yyyy =
     * year MM = month dd = day hh = hour mm = minutes ss = seconds SSS =
     * milliseconds
     * @throws event.RabbitMQInterface.ParticipantNotRegisteredException
     */
    public String getRegistrationTime() throws RabbitMQInterface.ParticipantNotRegisteredException {
        
        

        if (participantId != -1) {
            
            TimeStamp.setTimeInMillis(participantId);
            
            return (TimeStampFormat.format(TimeStamp.getTime()));

        } else {

            throw new RabbitMQInterface.ParticipantNotRegisteredException("Participant not registered");

        } // if

    } // getRegistrationTime

    //consumir mensajes
    public void suscribeMsg(String queue1, String severity) {
            
        try {
            channel = connection.createChannel();
                
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, sending, "");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    message = new String(body, "UTF-8");

                }
            };
            
            channel.basicConsume(queue, true, consumer);
            
            
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    
    public String returnMessage(){
        
        return message;
    }
    
    //publicar mensajes
    public void publishMsg(String message, String queue1) throws Exception {  
        try {
            
            
            channel = connection.createChannel();
            
            channel.exchangeDeclare(sending, "fanout");
            
            
            channel.basicPublish(sending, "", null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");

            } catch (Throwable ex) {}
        
   
    }
    
    /**
     * This method returns the ID of the participant that posted this Event.
     *
     * @return long integer
     */
    public int getEventId() {
        String []values = this.message.split("&");
       if (values.length >= 3)
           
           try {
               return Integer.parseInt(values[1]);
           } catch (NumberFormatException e) {
               return -1;
           }
       
       return -1;
    } // getEventId
    
    /**
     * This method returns the message (if there is one) of the posted event.
     * There is not semantic imposed on event IDs.
     *
     * @return The message contained in this event
     */
    public String getMessage() {
       String []values = this.message.split("&");
        
       if (values.length >= 3)
          return values[0];
        
        return "";
    } // getMenssage
    
    public String getTime() {
       String []values = this.message.split("&");
        
       if (values.length >= 3)
          return values[2];
        
        return "";
    } // getTime
    
}