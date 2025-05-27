package listati;

import javafx.scene.control.CheckBox;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Prezzi {
    public static Map<String, Integer> prezziBaseVeicoli = new HashMap<>(Map.of(
            "Auto", 5,
            "Moto", 2,
            "Camion", 10,
            "Corriera", 15
    ));

    public static Map<Integer, Integer> prezziOpzioni = new HashMap<>(Map.of(
            1, 5,    //Custodia Parcheggio
            2, 30,      //Albergo
            3, 5,       //Accesso a disabili
            4, 1,       //Bagni
            5, 5,       //Area Picnic
            6, 50           //Pieno di Benzina/Gasolio/Metano
    ));

    public static Map<DayOfWeek, Integer> prezziGiorni = new HashMap<>(Map.of(
            DayOfWeek.MONDAY, 1,
            DayOfWeek.TUESDAY, 1,
            DayOfWeek.WEDNESDAY, 1,
            DayOfWeek.THURSDAY, 1,
            DayOfWeek.FRIDAY, 2,
            DayOfWeek.SATURDAY, 4,
            DayOfWeek.SUNDAY, 3
    ));

    public static double costoOrario = 0.15;

    public static int getPrezzo(String tipo) {
        return prezziBaseVeicoli.getOrDefault(tipo, 0);
    }
    public static int getPrezzoOpzioni(int opzione) {
        return prezziOpzioni.getOrDefault(opzione, 0);
    }
    public static int getPrezzoGiorno(DayOfWeek giorno) {
        return prezziGiorni.getOrDefault(giorno, 0);
    }

    public static void setPrezzoVeicolo(String tipo, int prezzo) {
        if (tipo != null && prezzo >= 0) prezziBaseVeicoli.put(tipo, prezzo);
    }
    public static void setPrezzoOpzione(int opzione, int prezzo) {
        if (prezzo >= 0) prezziOpzioni.put(opzione, prezzo);
    }
    public static void setPrezzoGiorno(DayOfWeek giorno, int prezzo) {
        if (giorno != null && prezzo >= 0) prezziGiorni.put(giorno, prezzo);
    }
    public static void setCostoOrario(double costo) {
        if (costo >= 0) costoOrario = costo;
    }

    public static double getPrezzoOra(LocalDateTime arrivo, LocalDateTime partenza) {
        long differenzaMinuti = java.time.Duration.between(arrivo, partenza).toMinutes();
        return ((double) differenzaMinuti / 15) * costoOrario;
    }

    public static int getPrezzoGiorno(LocalDate data) {
        if (data == null) return 0;
        return prezziGiorni.getOrDefault(data.getDayOfWeek(), 0);
    }

    public static double calcolaTotale(String tipo, LocalDate data, LocalDateTime orarioArrivo, LocalDateTime orarioPartenza, boolean extra, CheckBox[] opzioni) {
        int base = getPrezzo(tipo);
        int giorno = (data != null) ? getPrezzoGiorno(data) : 0;
        double ora = (orarioArrivo != null && orarioPartenza != null && orarioPartenza.isAfter(orarioArrivo))
                ? getPrezzoOra(orarioArrivo, orarioPartenza)
                : 0;
        int costoExtra = 0;
        if (extra && opzioni != null) {
            for (int i = 0; i < opzioni.length; i++) {
                if (opzioni[i].isSelected()) {
                    costoExtra += getPrezzoOpzioni(i + 1);
                }
            }
        }
        double totale = base + giorno + ora + costoExtra;
        return arrotonda(totale);
    }

    public static double arrotonda(double valore) {
        return new BigDecimal(valore).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
