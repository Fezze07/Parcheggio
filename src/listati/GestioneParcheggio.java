package listati;

import java.sql.Connection;
import java.sql.SQLException;

public class GestioneParcheggio {
    private static Connection conn;

    public static void inizializzaConnessione() {
        try {
            conn = Connessione_SQL.creaConnessione();
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database esterno");
        }
    }

    public static void prenota(Posto posto) {
        SalvaCarica.esportaParcheggio(posto);
    }

    public static void cancellaUtente(Utente utente) {
        FunzioniAdmin.eliminaPrenotazioneUtente(utente, conn);
    }

    public static void visualizzaParcheggio(Utente utente) {
        GUI_VisualizzaParcheggio.mostraParcheggioScegliOra(utente);
    }

    public static void caricaOrari() {
        SalvaCarica.caricaOrari();
    }

    public static void caricaPrezzi() {
        SalvaCarica.caricaPrezzi();
    }

    public static void visualizzaPrenotazioniAttive(Utente utente) {
        FunzioniAdmin.visualizzaPrenotazioniAttive(utente, conn);
    }
}