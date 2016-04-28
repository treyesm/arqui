
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.ParseException;
import java.util.Date;

public class MMSConsole extends JFrame {

    Color colorDoor = Color.BLACK;
    Color colorWindow = Color.BLACK;
    Color colorMotion = Color.BLACK;
    Color colorSprinklers = Color.BLACK;
    Color colorFire = Color.BLACK;

    public MMSConsole() {
        super("Muma Console");

        //Set default close operation for JFrame  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set JFrame size  
        setSize(600, 350);

        //Make JFrame visible   
        super.setVisible(true);

    }

    public void drawCircle(int x, int y) {
        Graphics g = this.getGraphics();
        g.setColor(colorDoor);
        g.fillOval(x + 40, y + 50, 22, 22);
        g.setColor(colorWindow);
        g.fillOval(x + 230, y + 50, 22, 22);
        g.setColor(colorMotion);
        g.fillOval(x + 450, y + 50, 22, 22);

        g.setColor(colorFire);
        g.fillOval(x + 40, y + 230, 22, 22);

        g.setColor(colorSprinklers);
        g.fillOval(x + 450, y + 230, 22, 22);

        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Sensors", x, y + 30);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("Door Sensor", x + 10, y + 90);
        g.drawString("Door Description", x, y + 110);
        g.drawString("Window Sensor", x + 190, y + 90);
        g.drawString("Window Description", x + 180, y + 110);
        g.drawString("Motion Detection Sensor", x + 380, y + 90);
        g.drawString("Motion Description", x + 410, y + 110);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Fire alarm", x + 10, y + 210);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Sprinklers", x + 410, y + 210);

        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("Fire description", x, y + 270);
        g.drawString("Sprinklers description", x + 400, y + 270);

    }

    public void paint(Graphics g) {

        drawCircle(20, 20);

    }

    public static void main(String args[]) {
        boolean isDone = false;
        int delay = 5000;
        EASMonitor monitorSensors;                     // The environmental control system monitor

        if (args.length != 0) {
            // event manager is not on the local system
            monitorSensors = new EASMonitor(args[0]);
        } else {
            monitorSensors = new EASMonitor();
        } // if

        MMSConsole f = new MMSConsole();

        f.repaint();
        // Here we check to see if registration worked. If ef is null then the
        // event manager interface was not properly created.
        if (monitorSensors.isRegistered()) {
            monitorSensors.start();
            while (!isDone) {
                // Here we start the monitoring and control thread
                String[] arrSensores = new String[4];
                arrSensores = monitorSensors.returnState();
                System.out.println(arrSensores[0] + arrSensores[1] + arrSensores[2] + arrSensores[3]);

                if (f.comparaHoras(arrSensores[0]) > 4000) {
                    f.colorDoor = Color.RED;
                } else {
                    f.colorDoor = Color.GREEN;
                }
                if (f.comparaHoras(arrSensores[1]) > 4000) {
                    f.colorWindow = Color.RED;
                } else {
                    f.colorWindow = Color.GREEN;
                }
                if (f.comparaHoras(arrSensores[2]) > 4000) {
                    f.colorMotion = Color.RED;
                } else {
                    f.colorMotion = Color.GREEN;
                }
                if (f.comparaHoras(arrSensores[3]) > 4000) {
                    f.colorFire = Color.RED;
                } else {
                    f.colorFire = Color.GREEN;
                }
                f.repaint();
                try {
                    Thread.sleep(delay);
                } // try
                catch (Exception e) {
                    System.out.println("Sleep error:: " + e);
                } // catch
            }
        } else {
            System.out.println("\n\nUnable start the monitor.\n\n");
        } // if
    } // main

    private long comparaHoras(String fechaString) {
        long diferencia = 0;
        if (fechaString != null) {
            Calendar TimeStamp = Calendar.getInstance();
            SimpleDateFormat TimeStampFormat2 = new SimpleDateFormat("hh:mm:ss");
            String tiempo = TimeStampFormat2.format(TimeStamp.getTime());
            Date fechaFinal = new Date();
            System.out.println("fecha"+fechaString+tiempo);
            try {
                fechaFinal = TimeStampFormat2.parse(tiempo);
            } catch (ParseException ex) {
            }

            Date fechaInicial = new Date();
            try {
                fechaInicial = TimeStampFormat2.parse(fechaString);
            } catch (ParseException ex) {
            }

            long fechaInicialMs = fechaInicial.getTime();
            long fechaFinalMs = fechaFinal.getTime();
            diferencia = fechaFinalMs - fechaInicialMs;
            System.out.println("resta"+diferencia);
        }

        return diferencia;
    }
  
} // ECSConsole

