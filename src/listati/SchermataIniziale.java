package listati;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.Objects;

public class SchermataIniziale {
    private static Utente utenteLoggato = null;
    private static BorderPane base;
    public static StackPane root;
    public static void setBase(BorderPane root) {
        base = root;
    }
    public static void setRoot(StackPane root2) {
        root = root2;
    }

    public static void mostraMenuIniziale() {
        ImageView logo = new ImageView(new Image(Objects.requireNonNull(GUI_GestioneUtenti.class.getResource("/risorse/immagini/logo.png")).toExternalForm()));
        Animazioni.creaLogo(logo);
        VBox boxLogo = new VBox(logo);
        boxLogo.setAlignment(Pos.BOTTOM_LEFT);
        boxLogo.setPadding(new Insets(0, 0, 10, 10));

        Label titolo = new Label("Cisera Cars");
        titolo.getStyleClass().add("titolo");
        Label sottotitolo = new Label("La sosta in vera e totale sicurezza!");
        sottotitolo.getStyleClass().add("sottotitolo");
        sottotitolo.setPadding(new Insets(0, 0, 10, 0));
        Button pulsChiSiamo = InterfacciaHelper.creaPulsante("Chi siamo?", _ -> Animazioni.mostraInfoChiSiamo());
        pulsChiSiamo.getStyleClass().add("chi-siamo");
        VBox boxTitolo = new VBox(titolo, sottotitolo, pulsChiSiamo);
        boxTitolo.setAlignment(Pos.CENTER);
        boxTitolo.getStyleClass().add("vbox-titolo");

        VBox boxOrologio = InterfacciaHelper.creaBoxDataOra();
        boxOrologio.setAlignment(Pos.CENTER);
        boxOrologio.getStyleClass().add("vbox-orologio");

        VBox menuPulsanti = creaMenuUtente(utenteLoggato);
        menuPulsanti.setAlignment(Pos.CENTER);
        menuPulsanti.setPadding(new Insets(10));
        menuPulsanti.getStyleClass().add("vbox-menu");
        menuPulsanti.setMinWidth(300);

        StackPane boxParcheggio = new StackPane();
        boxParcheggio.setMaxSize(200, 200);
        boxParcheggio.getStyleClass().add("stack-parcheggio");
        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(0), _ -> boxParcheggio.getChildren().setAll(GUI_VisualizzaParcheggio.creaBoxParcheggioLive())),
                new KeyFrame(Duration.seconds(10))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        VBox boxParcheggioLive = new VBox(boxParcheggio);
        boxParcheggioLive.setAlignment(Pos.CENTER);
        boxParcheggioLive.setPadding(new Insets(20));
        boxParcheggioLive.getStyleClass().add("vbox-parcheggio");

        HBox boxCentrale = new HBox(50, menuPulsanti, boxParcheggioLive);
        boxCentrale.setAlignment(Pos.CENTER);
        boxCentrale.setPadding(new Insets(20));

        VBox boxContenuti = new VBox(20, boxCentrale);
        boxContenuti.setAlignment(Pos.CENTER);
        boxContenuti.getStyleClass().add("hbox-contenitore");
        boxContenuti.setMaxSize(1500, 1000);
        boxContenuti.getStyleClass().add("vbox-base");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        Region spazioCentrale = new Region();
        HBox.setHgrow(spazioCentrale, Priority.ALWAYS);
        topBar.getChildren().addAll(boxTitolo, spazioCentrale, boxOrologio);

        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(boxContenuti);
        layout.setLeft(boxLogo);
        BorderPane.setAlignment(boxLogo, Pos.BOTTOM_LEFT);
        layout.setPadding(new Insets(10));
        layout.getStyleClass().add("border-layout");
        base.setCenter(layout);
    }

    public static VBox creaMenuUtente(Utente utente) {
        VBox menu = new VBox(20);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(15));
        menu.getStyleClass().add("vbox-menu");

        // Pulsanti base per tutti
        Button pulsLogout = InterfacciaHelper.creaPulsante("Logout", _ -> aggiornaMenu(null));

        if (utente == null) {
            Button pulsAccedi = InterfacciaHelper.creaPulsante("Accedi", _ -> GUI_GestioneUtenti.accedi(SchermataIniziale::aggiornaMenu));
            Button pulsRegistrati = InterfacciaHelper.creaPulsante("Registrati", _ -> GUI_GestioneUtenti.registrati(SchermataIniziale::aggiornaMenu));
            Button pulsPrenotaPosto = creaPulsanteProtetto("Prenota un posto", "prenotazione");
            Button pulsVerificaDisponibilita = InterfacciaHelper.creaPulsante("Verifica disponibilità", _ -> GestioneParcheggio.visualizzaParcheggio(null));
            menu.getChildren().addAll(pulsAccedi, pulsRegistrati, pulsPrenotaPosto, pulsVerificaDisponibilita);
        } else {
            if ("Utente".equals(utente.getTipo())) {
                Button pulsPrenotaPosto = creaPulsanteProtetto("Prenota un posto", "prenotazione");
                Button pulsVerificaDisponibilita = InterfacciaHelper.creaPulsante("Verifica disponibilità", _ -> GestioneParcheggio.visualizzaParcheggio(utente));
                Button pulsDisdirePrenotazione = creaPulsanteProtetto("Disdici prenotazione", "disdici");
                Button pulsCambiaPassword = creaPulsanteProtetto("Cambia Password", "cambiaPassword");
                Button pulsCambiaNomeUtente = creaPulsanteProtetto("Cambia Nome Utente", "cambiaNome");
                menu.getChildren().addAll(pulsPrenotaPosto, pulsVerificaDisponibilita, pulsDisdirePrenotazione, pulsCambiaPassword, pulsCambiaNomeUtente);
            } else {
                Button pulsSimulaPrenotazione = InterfacciaHelper.creaPulsante("Simula prenotazione", _ -> GUI_Prenotazione.mostraFinestraPrenotazione(utente, null));
                Button pulsCancellaUtenti = InterfacciaHelper.creaPulsante("Cancella utente", _ -> GestioneUtenti.cancellaUtenteAdmin(utente));
                Button pulsVisualizzaPrenotazioniAttive = InterfacciaHelper.creaPulsante("Visualizza prenotazioni attive", _ -> GestioneParcheggio.visualizzaPrenotazioniAttive(utente));
                Button pulsModificaOrari = InterfacciaHelper.creaPulsante("Modifica orari", _ -> FunzioniAdmin.modificaApertura(utente));
                Button pulsGestioneTariffe = InterfacciaHelper.creaPulsante("Gestione tariffe", _ -> FunzioniAdmin.modificaTariffe(utente));
                Button pulsChiudiGiorni = InterfacciaHelper.creaPulsante("Chiudi Giorni", _ -> FunzioniAdmin.inserisciDateChiusura(utente));
                menu.getChildren().addAll(pulsSimulaPrenotazione, pulsCancellaUtenti, pulsVisualizzaPrenotazioniAttive, pulsModificaOrari, pulsGestioneTariffe, pulsChiudiGiorni);
            }
        }
        menu.getChildren().addAll(pulsLogout);
        return menu;
    }

    public static void aggiornaMenu(Utente utente) {
        utenteLoggato = utente;
        mostraMenuIniziale();
    }

    private static Button creaPulsanteProtetto(String testo, String azione) {
        return InterfacciaHelper.creaPulsante(testo, _ -> {
            if (utenteLoggato == null) {
                InterfacciaHelper.mostraErrore("Esegui prima l'accesso!");
            } else {
                switch (azione) {
                    case "prenotazione" -> GUI_Prenotazione.mostraFinestraPrenotazione(utenteLoggato, null);
                    case "disdici" -> GestioneParcheggio.cancellaUtente(utenteLoggato);
                    case "cambiaPassword" -> GUI_GestioneUtenti.cambiaPassword(utenteLoggato);
                    case "cambiaNome" -> GUI_GestioneUtenti.cambiaNomeUtente(utenteLoggato);
                }
            }
        });
    }
}
