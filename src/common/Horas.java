/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author tania
 */
public class Horas {

    public long comparaHoras(String fechaString) {
        long diferencia = 0;
        if (fechaString != null) {
            Calendar TimeStamp = Calendar.getInstance();
            SimpleDateFormat TimeStampFormat2 = new SimpleDateFormat("hh:mm:ss");
            String tiempo = TimeStampFormat2.format(TimeStamp.getTime());
            Date fechaFinal = new Date();
            System.out.println("fecha" + fechaString + tiempo);
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
            System.out.println("resta" + diferencia);
        }

        return diferencia;
    }
}
