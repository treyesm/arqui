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

/**
 *
 * @author Equipo de Zelma
 */
public class RabbitMQInterface {

    private static final String sending = "logs";
    private static String message = "";
    Connection connection;

    public RabbitMQInterface() {

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");

        /*factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost ("localhost");
        factory.setPort(15672);*/
        try {

            try {

                connection = factory.newConnection();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    //consumir mensajes
    public void suscribeMsg() {

        try {
            Channel channel = connection.createChannel();

            String queueSignals = channel.queueDeclare().getQueue();
            channel.queueBind(queueSignals, sending, "");

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    message = new String(body, "UTF-8");

                    System.out.println(" [x] Received '" + message + "'");
                }
            };
            channel.basicConsume(queueSignals, true, consumer);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String returnMessage(){
        System.out.println(" [*] mensaje"+message);
        return message;
    }
    //publicar mensajes
    public void publishMsg(String message) {
        //String msg[] = {"Avila", "Burgos", "León", "Palencia", "Salamanca"};

        try {

            Channel channel = connection.createChannel();
            channel.exchangeDeclare(sending, "fanout");
            channel.basicPublish(sending, "", null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");

        } catch (IOException e) {
            e.printStackTrace();
            }
    }
    
}
   

