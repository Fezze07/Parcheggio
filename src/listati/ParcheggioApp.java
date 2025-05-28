package listati;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Objects;

public class ParcheggioApp extends Application {
    static BorderPane base;
    StackPane root;

    @Override
    public void start(Stage primaryStage) {
        base = new BorderPane();
        root = new StackPane();
        root.getChildren().add(base);
        Scene scene = new Scene(root);
        applicaStile(scene);
        InterfacciaHelper.creaStage(primaryStage, scene);
        inizializzaRootPane(base, root);

        GestioneUtenti.inizializzaConnessione();
        GestioneParcheggio.inizializzaConnessione();
        GestioneParcheggio.caricaPrezzi();
        GestioneParcheggio.caricaOrari();

        SchermataIniziale.mostraMenuIniziale();
    }

    private void applicaStile(Scene scene) {
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/interfacciaHelper.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/menuIniziale.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/autenticazione.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/prenotazione.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/listino.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/confermaPrenotazione.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/visualizzaParcheggio.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/scegliOraParcheggio.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/risorse/css/modificaTariffe.css")).toExternalForm());
    }

    public static void inizializzaRootPane(BorderPane base, StackPane root) {
        InterfacciaHelper.setBase(base);
        GUI_Prenotazione.setBase(base);
        GUI_VisualizzaParcheggio.setBase(base);
        GUI_GestioneUtenti.setBase(base);
        ModificaPrenotazione.setBase(base);
        Animazioni.setBase(base);
        FunzioniAdmin.setBase(base);
        SchermataIniziale.setBase(base);
        SchermataIniziale.setRoot(root);
        GUI_Prenotazione.setRoot(root);
        GUI_VisualizzaParcheggio.setRoot(root);
    }

    public static void main(String[] args) {
        Connessione_SQL.testConnessione();
        launch(args);
    }
}
