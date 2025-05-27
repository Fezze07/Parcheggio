package listati;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class GUI_GestioneUtenti {
    public static GestioneUtenti gestioneUtenti = new GestioneUtenti();
    private static BorderPane base;
    public static void setBase(BorderPane root) {
        base = root;
    }

    public static void accedi(Consumer<Utente> callback) {
        // Crea i campi per inserire i dati
        Label labelNome = InterfacciaHelper.creaLabel("Nome Utente");
        TextField campoNome = InterfacciaHelper.creaCampoTesto("");
        Label labelPassword = InterfacciaHelper.creaLabel("Password");
        PasswordField campoPassword = InterfacciaHelper.creaCampoPassword("");

        // Pulsante per accedere
        Button accedi = InterfacciaHelper.creaPulsante("Accedi", _ -> {
            String nome = campoNome.getText();
            String password = campoPassword.getText();
            if (gestioneUtenti.verificaCredenziali(nome, password)) {
                Utente u = gestioneUtenti.ritornaUtente(nome);
                callback.accept(u);
            }
        });
        Button indietro = InterfacciaHelper.creaPulsante("Non ancora registrato, fallo ora qua sotto!", _ -> registrati(callback));
        indietro.getStyleClass().setAll("indietro");
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.mostraMenuIniziale());
        HBox pulsantiBox = new HBox(40, accedi, esci);
        pulsantiBox.setAlignment(Pos.CENTER);

        // Layout per la finestra di prenotazione
        VBox layout = new VBox(15,labelNome, campoNome, labelPassword, campoPassword, pulsantiBox, indietro);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        base.setCenter(layout);
    }

    public static void registrati(Consumer<Utente> callback) {
        // Crea i campi per inserire i dati
        Label labelNome = InterfacciaHelper.creaLabel("Nome Utente");
        TextField campoNome = InterfacciaHelper.creaCampoTesto("");
        Label labelPassword = InterfacciaHelper.creaLabel("Password");
        PasswordField campoPassword = InterfacciaHelper.creaCampoPassword("");

        // Pulsante per confermare la prenotazione
        Button registrati = InterfacciaHelper.creaPulsante("Registrati", _ -> {
            String nome = campoNome.getText();
            String password = campoPassword.getText();
            if (nome.isEmpty() || password.isEmpty()) {
                InterfacciaHelper.mostraErrore("Nome utente e password non possono essere vuoti!");
            } else if (gestioneUtenti.verificaDisponibilitaNome(nome)) {
                InterfacciaHelper.mostraErrore("Nome utente già in uso!");
            } else {
                Utente utenteLoggato = new Utente("Utente", nome, password);
                gestioneUtenti.aggiungiUtente(utenteLoggato);
                InterfacciaHelper.mostraConferma("Registrazione completata! Benvenuto, " + utenteLoggato.getNomeUtente());
                callback.accept(utenteLoggato);
            }
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.mostraMenuIniziale());
        HBox pulsantiBox = new HBox(40, registrati, esci);
        pulsantiBox.setAlignment(Pos.CENTER);
        VBox layout = new VBox(10, labelNome, campoNome, labelPassword, campoPassword, pulsantiBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        base.setCenter(layout);
    }

    public static void cambiaPassword(Utente utente) {
        Label labelPassword = InterfacciaHelper.creaLabel("Nuova password!");
        PasswordField campoPassword = InterfacciaHelper.creaCampoPassword("");

        final Button[] salva = new Button[1];
        salva[0] = InterfacciaHelper.creaPulsante("Salva Nuova Password", _ -> {
            String nuovaPassword = campoPassword.getText().trim();
            if (nuovaPassword.isEmpty()) {
                InterfacciaHelper.mostraErrore("La password non può essere vuota!");
            } else {
                GestioneUtenti.cambiaPassword(utente, nuovaPassword);
                InterfacciaHelper.mostraConfermaRunnable("Password cambiata con successo!", () -> SchermataIniziale.aggiornaMenu(utente));
            }
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        VBox layout = new VBox(10, labelPassword, campoPassword, salva[0], esci);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        base.setCenter(layout);
    }

    public static void cambiaNomeUtente(Utente utente) {
        Label labelNome = InterfacciaHelper.creaLabel("Nuovo nome utente");
        TextField campoNome = InterfacciaHelper.creaCampoTesto("");

        final Button[] salva = new Button[1];
        salva[0] = InterfacciaHelper.creaPulsante("Salva Nuovo Nome", _ -> {
            String nuovoNome = campoNome.getText().trim();
            if (nuovoNome.isEmpty()) {
                InterfacciaHelper.mostraErrore("Il nome utente non può essere vuoto!");
            } else if (gestioneUtenti.verificaDisponibilitaNome(nuovoNome)) {
                InterfacciaHelper.mostraErrore("Nome utente già in uso!");
            } else {
                gestioneUtenti.cambiaNomeUtente(utente, nuovoNome);
                InterfacciaHelper.mostraConfermaRunnable("Nome utente cambiato con successo!", () -> SchermataIniziale.aggiornaMenu(utente));
            }
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        VBox layout = new VBox(10, labelNome, campoNome, salva[0], esci);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        base.setCenter(layout);
    }
}