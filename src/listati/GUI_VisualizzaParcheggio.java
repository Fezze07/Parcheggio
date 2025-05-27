package listati;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class GUI_VisualizzaParcheggio {
    private static final int GRANDEZZA_X = 5;
    private static final int GRANDEZZA_Y = 5;
    private static final Random rand = new Random();
    private static Button bottoneSelezionato = null;
    private static StackPane ultimoSlotSelezionato = null;
    private static BorderPane base;
    public static StackPane root;
    public static void setBase(BorderPane root) {
        base = root;
    }
    public static void setRoot(StackPane root2) {
        root = root2;
    }

    public static void scegliPosto(LocalDateTime arrivo, LocalDateTime partenza, Utente utente, DatiPrenotazioneTemp temp, Consumer<int[]> callback) {
        final int[] selezionato = {-1, -1};
        GridPane griglia = new GridPane();
        griglia.getStyleClass().add("griglia-parcheggio");
        Set<String> occupati = GestioneVisualizzazioneParcheggio.getPostiOccupati(arrivo, partenza);
        int nPosto = 1;
        for (int i = 0; i < GRANDEZZA_X; i++) {
            for (int j = 0; j < GRANDEZZA_Y; j++) {
                StackPane posto = creaPostoGrafico(i, j, occupati, selezionato, nPosto, true);
                griglia.add(posto, j, i);
                nPosto++;
            }
        }
        Button conferma = creaPulsanteConferma(selezionato, utente, temp, callback);
        VBox layout = new VBox(10, griglia, conferma);
        layout.getStyleClass().add("selezione-posto-container");
        base.setCenter(layout);
    }

    public static void mostraParcheggioScegliOra(Utente utente) {
        VBox layout = new VBox(20);
        layout.getStyleClass().add("selezione-orari");

        Label titolo = new Label("Seleziona orari prenotazione");
        titolo.getStyleClass().add("titolo-seleziona-orari");
        Label dataArrivoLabel = new Label("Data :");
        dataArrivoLabel.getStyleClass().add("etichetta-orari");
        DatePicker dataArrivoPicker = InterfacciaHelper.creaDatePicker();

        dataArrivoPicker.getStyleClass().add("scegliOra-datapicker");
        Label orarioArrivoLabel = new Label("Orario di arrivo :");
        orarioArrivoLabel.getStyleClass().add("etichetta-orari");
        Slider sliderArrivo = InterfacciaHelper.creaSliderOrario(null);
        sliderArrivo.setMaxWidth(400);

        Label orarioArrivoSelezionato = new Label(InterfacciaHelper.getOrarioFromSlider(sliderArrivo.getValue()));
        orarioArrivoSelezionato.getStyleClass().add("etichetta-orari");
        sliderArrivo.valueProperty().addListener((_, _, newVal) ->
                orarioArrivoSelezionato.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue()))
        );
        Label orarioPartenzaLabel = new Label("Orario di partenza:");
        orarioPartenzaLabel.getStyleClass().add("etichetta-orari");
        Slider sliderPartenza = InterfacciaHelper.creaSliderOrario(null);
        sliderPartenza.setMaxWidth(400);
        Label orarioPartenzaSelezionato = new Label(InterfacciaHelper.getOrarioFromSlider(sliderPartenza.getValue()));
        orarioPartenzaSelezionato.getStyleClass().add("etichetta-orari");
        sliderPartenza.valueProperty().addListener((_, _, newVal) ->
                orarioPartenzaSelezionato.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue()))
        );

        Button mostra = new Button("Mostra Parcheggio");
        mostra.getStyleClass().add("pulsante-grande");
        mostra.setOnAction(_ -> {
            LocalDate dataA = dataArrivoPicker.getValue();
            if (dataA == null) {
                mostraMessaggio("Seleziona la data!", true,() -> GUI_Prenotazione.mostraFinestraPrenotazione(utente, null));
                return;
            }
            LocalDateTime arrivo = InterfacciaHelper.creaDataOrario(dataA, sliderArrivo);
            LocalDateTime partenza = InterfacciaHelper.creaDataOrario(dataA, sliderPartenza);
            if (!arrivo.isBefore(partenza)) {
                mostraMessaggio("L'orario di partenza deve essere dopo quello di arrivo!", true,() -> GUI_Prenotazione.mostraFinestraPrenotazione(utente, null));
                return;
            }
            mostraParcheggio(arrivo, partenza, utente);
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        layout.getChildren().addAll(
                titolo, dataArrivoLabel, dataArrivoPicker,
                orarioArrivoLabel, sliderArrivo, orarioArrivoSelezionato,
                orarioPartenzaLabel, sliderPartenza, orarioPartenzaSelezionato, mostra, esci
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        base.setCenter(layout);
    }


    public static void mostraParcheggio(LocalDateTime arrivo, LocalDateTime partenza, Utente utente) {
        GridPane griglia = new GridPane();
        griglia.getStyleClass().add("griglia-parcheggio");
        Set<String> occupati = GestioneVisualizzazioneParcheggio.getPostiOccupati(arrivo, partenza);
        int nPosto = 1;
        for (int i = 0; i < GRANDEZZA_X; i++) {
            for (int j = 0; j < GRANDEZZA_Y; j++) {
                StackPane posto = creaPostoGrafico(i, j, occupati, null, nPosto, false);
                griglia.add(posto, j, i);
                nPosto++;
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm").withLocale(Locale.ITALIAN);
        String testo = "🕒 Parcheggio da: " + arrivo.format(formatter) + " a " + partenza.format(formatter);
        Label labelPeriodo = new Label(testo);
        labelPeriodo.getStyleClass().add("etichetta-periodo");
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));

        VBox layout = new VBox(20, labelPeriodo,  griglia, esci);
        layout.getStyleClass().add("visualizza-parcheggio-container");
        base.setCenter(layout);
    }

    public static VBox creaBoxParcheggioLive() {
        LocalDateTime arrivo = LocalDateTime.now();
        LocalDateTime partenza = arrivo.plusHours(1);
        GridPane griglia = new GridPane();
        griglia.getStyleClass().add("griglia-parcheggio");
        Set<String> occupati = GestioneVisualizzazioneParcheggio.getPostiOccupati(arrivo, partenza);
        int nPosto = 1;
        for (int i = 0; i < GRANDEZZA_X; i++) {
            for (int j = 0; j < GRANDEZZA_Y; j++) {
                StackPane posto = creaPostoGrafico(i, j, occupati, null, nPosto, false);
                griglia.add(posto, j, i);
                nPosto++;
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'alle' HH:mm").withLocale(Locale.ITALIAN);
        String testo = "🕒 Parcheggio da: " + arrivo.format(formatter) + " a " + partenza.format(formatter);
        Label labelPeriodo = new Label(testo);
        labelPeriodo.getStyleClass().add("etichetta-periodo");
        VBox boxParcheggio = new VBox(10, labelPeriodo, griglia);
        boxParcheggio.getStyleClass().add("visualizza-parcheggio-container");
        return boxParcheggio;
    }

    private static Button creaPulsanteConferma(int[] selezionato, Utente utente, DatiPrenotazioneTemp temp, Consumer<int[]> callback) {
        Button conferma = new Button("Conferma Prenotazione");
        conferma.getStyleClass().add("bottone-visualizza-conferma");
        conferma.setOnAction(_ -> {
            if (selezionato[0] != -1 && selezionato[1] != -1) {
                int numeroPosto = GestioneVisualizzazioneParcheggio.calcolaNumeroPosto(selezionato[0], selezionato[1]);

                GUI_Prenotazione.xPosto = selezionato[0];
                GUI_Prenotazione.yPosto = selezionato[1];
                temp.XPosto = selezionato[0];
                temp.YPosto = selezionato[1];
                callback.accept(new int[]{selezionato[0], selezionato[1]});
                GUI_Prenotazione.mostraFinestraPrenotazione(utente, temp);
                mostraMessaggio("Posto " + numeroPosto + " selezionato!", false, null);
                if (bottoneSelezionato != null) {
                    bottoneSelezionato.getStyleClass().remove("posto-selezionato");
                    bottoneSelezionato.getStyleClass().add("posto-libero");
                    bottoneSelezionato = null;
                }
                selezionato[0] = -1;
                selezionato[1] = -1;
            } else {
                mostraMessaggio("Seleziona un posto prima!", true, null);
            }
        });
        return conferma;
    }

    private static StackPane creaPostoGrafico(int i, int j, Set<String> occupati, final int[] selezionato, int numeroPosto, boolean cliccabile) {
        StackPane slot = new StackPane();
        slot.setPrefSize(100, 120);
        slot.getStyleClass().add("posto-slot");

        Image sfondoImg = new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_vuoto.png")));
        ImageView sfondo = new ImageView(sfondoImg);
        sfondo.setFitWidth(100);
        sfondo.setFitHeight(100);

        VBox contenuto = new VBox(4);
        contenuto.setAlignment(Pos.CENTER);
        boolean occupato = GestioneVisualizzazioneParcheggio.isOccupato(i, j, occupati);
        if (occupato) {
            Image macchinaImg = macchineImg.get(rand.nextInt(macchineImg.size()));
            ImageView macchina = new ImageView(macchinaImg);
            macchina.setFitWidth(50);
            macchina.setFitHeight(80);
            StackPane immagineConMacchina = new StackPane(sfondo, macchina);
            contenuto.getChildren().add(immagineConMacchina);
        } else {
            contenuto.getChildren().add(sfondo);
        }
        Text numero = new Text(String.valueOf(numeroPosto));
        numero.getStyleClass().add("numero-posto");
        contenuto.getChildren().add(numero);
        if (cliccabile) {
            slot.setOnMouseClicked(_ -> {
                if (ultimoSlotSelezionato != null)
                    ultimoSlotSelezionato.getStyleClass().remove("posto-selezionato");
                slot.getStyleClass().add("posto-selezionato");
                ultimoSlotSelezionato = slot;
                selezionato[0] = i;
                selezionato[1] = j;
            });
            slot.getStyleClass().add("posto-cliccabile");
        }
        slot.getChildren().add(contenuto);
        return slot;
    }

    private static final List<Image> macchineImg = List.of(
            new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_occupato1.png"))),
            new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_occupato2.png"))),
            new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_occupato3.png"))),
            new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_occupato4.png"))),
            new Image(Objects.requireNonNull(GUI_VisualizzaParcheggio.class.getResourceAsStream("/risorse/immagini/parcheggio_occupato5.png")))
    );

    private static void mostraMessaggio(String messaggio, boolean errore, Runnable azioneDopo) {
        if (errore) {
            InterfacciaHelper.mostraErrore(messaggio);
        } else {
            if (azioneDopo == null) azioneDopo = () -> {};
            InterfacciaHelper.mostraConfermaRunnable(messaggio, azioneDopo);
        }
    }
}