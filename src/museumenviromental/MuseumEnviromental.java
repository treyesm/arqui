/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package museumenviromental;

import event.RabbitMQInterface;
import java.util.Timer;
/**
 *
 * @author paco
 */
public class MuseumEnviromental {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int evtId=-5;
        String m="texto";
        String idstring= String.valueOf(evtId);
        String msg= idstring+m;
        System.out.println(" Cadena  completa= " + msg );
        
        
        
        int ev=0;
        String num =msg.substring(0, 1);
        ev=Integer.parseInt(num);
        System.out.println(" num= " + num.toString() );
        
        String text= msg.substring(2, msg.length());
        System.out.println(" text= " + text.toString() );
        
        
        RabbitMQInterface conn= new RabbitMQInterface();
        //    conn.suscribeMsg("");
        
        try {
            Thread.sleep(3000);
            } catch (InterruptedException ie) {
            //Handle exception
            }
        
        //conn.publishMsg();
        
        
        
        
        
    }
    
}
