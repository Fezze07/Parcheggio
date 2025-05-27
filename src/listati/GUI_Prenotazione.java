package listati;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class GUI_Prenotazione {
    private static final Label labelCosto = new Label();
    private static Slider sliderArrivo;
    private static Slider sliderPartenza;
    public static int xPosto = -1;
    public static int yPosto = -1;
    private static BorderPane base;
    public static StackPane root;

    public static void setBase(BorderPane root) {
        base = root;
    }
    public static void setRoot(StackPane root2) {
        root = root2;
    }

    public static void mostraFinestraPrenotazione(Utente utente, DatiPrenotazioneTemp temp) {
        HBox layout = new HBox(30);
        VBox formBox = creaFormPrenotazione(utente, temp, layout);
        VBox tabellaPrezziBox = creaTabellaPrezzi();
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(formBox, tabellaPrezziBox);
        base.setCenter(layout);
    }

    private static VBox creaFormPrenotazione(Utente utente, DatiPrenotazioneTemp temp, HBox layout) {
        VBox formBox = new VBox(20);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.TOP_CENTER);
        formBox.setPrefWidth(900);

        HBox labelNome = InterfacciaHelper.creaLabelConIcona("Inserisci il tuo nome e cognome:", "icona-nome.png");
        TextField campoNome = InterfacciaHelper.creaCampoTesto("Nome");
        TextField campoCognome = InterfacciaHelper.creaCampoTesto("Cognome");
        HBox labelTipo = InterfacciaHelper.creaLabelConIcona("Seleziona il tipo di veicolo con targa (es. AA123BB):", "icona-targa.png");
        ComboBox<String> comboTipo = InterfacciaHelper.creaComboTipo();
        TextField campoTarga = InterfacciaHelper.creaCampoTesto("Targa");
        HBox labelData = InterfacciaHelper.creaLabelConIcona("Seleziona la data della prenotazione:", "icona-data.png");
        DatePicker dataPicker = InterfacciaHelper.creaDatePicker();

        // ORARI
        sliderArrivo = InterfacciaHelper.creaSliderOrario(LocalDateTime.now());
        sliderPartenza = InterfacciaHelper.creaSliderOrario(LocalDateTime.now());
        sliderArrivo.setPrefWidth(350);
        sliderPartenza.setPrefWidth(350);

        Label titoloOrari = new Label("Inserisci l'orario di arrivo e partenza:");
        Label labelArrivo = new Label("Orario di arrivo:");
        Label orarioArrivo = new Label(InterfacciaHelper.getOrarioFromSlider(sliderArrivo.getValue()));
        Label labelPartenza = new Label("Orario di partenza:");
        Label orarioPartenza = new Label(InterfacciaHelper.getOrarioFromSlider(sliderPartenza.getValue()));
        orarioArrivo.setAlignment(Pos.CENTER);
        orarioArrivo.setMaxWidth(Double.MAX_VALUE);
        orarioPartenza.setAlignment(Pos.CENTER);
        orarioPartenza.setMaxWidth(Double.MAX_VALUE);

        VBox boxArrivo = new VBox(5, labelArrivo, sliderArrivo, orarioArrivo);
        VBox boxPartenza = new VBox(5, labelPartenza, sliderPartenza, orarioPartenza);
        HBox boxOrari = new HBox(30, boxArrivo, boxPartenza);
        boxOrari.setAlignment(Pos.CENTER);

        HBox labelOpzioni = InterfacciaHelper.creaLabelConIcona("Seleziona le opzioni extra:", "icona-opzioni.png");
        CheckBox checkBoxOpzioni = new CheckBox("Vuoi opzioni extra?");
        CheckBox[] opzioni = Stream.of("Custodia", "Albergo", "Disabili", "Bagni", "Picnic", "Pieno")
                .map(CheckBox::new).toArray(CheckBox[]::new);
        VBox boxOpzioniExtra = new VBox(10, opzioni);
        boxOpzioniExtra.setAlignment(Pos.CENTER);
        InterfacciaHelper.toggleBox(boxOpzioniExtra, false);
        checkBoxOpzioni.setOnAction(_ -> InterfacciaHelper.toggleBox(boxOpzioniExtra, checkBoxOpzioni.isSelected()));

        HBox labelPosto = InterfacciaHelper.creaLabelConIcona("Scegli il posto :", "icona-posto.png");
        Button scegliPosto = creaBottoneScegliPosto(dataPicker, utente, campoNome, campoCognome, campoTarga, comboTipo, checkBoxOpzioni, opzioni);
        riempiCampiTemp(temp, campoNome, campoCognome, campoTarga, comboTipo, dataPicker, checkBoxOpzioni, opzioni, boxOpzioniExtra, scegliPosto);

        HBox labelCostoTotale = InterfacciaHelper.creaLabelConIcona("Costo:", "icona-totale.png");
        labelCosto.setText("Costo della prenotazione: 0 euro");
        Button prenota = creaBottonePrenota(utente, campoNome, campoCognome, campoTarga, comboTipo, dataPicker, checkBoxOpzioni, opzioni);
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        HBox pulsanti = new HBox(40, prenota, esci);
        pulsanti.setAlignment(Pos.CENTER);

        formBox.getChildren().addAll(
                labelNome, campoNome, campoCognome,
                labelData, dataPicker, titoloOrari, boxOrari,
                labelTipo, comboTipo, campoTarga,
                labelPosto, scegliPosto,
                labelOpzioni, checkBoxOpzioni, boxOpzioniExtra,
                labelCostoTotale, labelCosto, pulsanti
        );

        impostaListener(comboTipo, dataPicker, checkBoxOpzioni, opzioni, scegliPosto, orarioArrivo, orarioPartenza);
        impostaMargini( campoNome, campoCognome, campoTarga, comboTipo, dataPicker, boxOrari,
                        checkBoxOpzioni, scegliPosto, boxOpzioniExtra, pulsanti);
            applicaStili(layout, formBox,
                campoNome, campoCognome, campoTarga, comboTipo, dataPicker,
                titoloOrari, labelArrivo, sliderArrivo, orarioArrivo,
                labelPartenza, sliderPartenza, orarioPartenza,
                checkBoxOpzioni, boxOpzioniExtra, opzioni,
                labelNome, labelData, labelTipo, labelPosto, labelOpzioni, labelCostoTotale);
        return formBox;
    }

    private static void applicaStili(HBox layout, VBox formBox,
                                     TextField campoNome, TextField campoCognome, TextField campoTarga,
                                     ComboBox<String> comboTipo, DatePicker dataPicker,
                                     Label titoloOrari, Label arrivo, Slider sliderArrivo, Label orarioArrivo,
                                     Label partenza, Slider sliderPartenza, Label orarioPartenza,
                                     CheckBox checkBoxOpzioni, VBox boxOpzioniExtra, CheckBox[] opzioni,
                                     HBox labelNomeBox, HBox labelDataBox, HBox labelTipoBox, HBox labelPostoBox, HBox labelOpzioniBox, HBox labelCostoBox ) {

        Label labelNome = (Label) labelNomeBox.getChildren().get(1);
        Label labelData = (Label) labelDataBox.getChildren().get(1);
        Label labelTipo = (Label) labelTipoBox.getChildren().get(1);
        Label labelPosto = (Label) labelPostoBox.getChildren().get(1);
        Label labelOpzioni = (Label) labelOpzioniBox.getChildren().get(1);
        Label labelCosto = (Label) labelCostoBox.getChildren().get(1);

        layout.getStyleClass().add("prenotazione-root");
        formBox.getStyleClass().add("form-box");
        layout.getStyleClass().add("form-layout")
        ;
        TextField[] campi = { campoNome, campoCognome, campoTarga };
        for (TextField campo : campi) campo.getStyleClass().add("prenotazione-input");

        Label[] labelOrari = {arrivo, orarioArrivo, partenza, orarioPartenza };
        for (Label l : labelOrari) l.getStyleClass().add("prenotazione-label-orario");
        Slider[] sliderOrari = { sliderArrivo, sliderPartenza };
        for (Slider s : sliderOrari) s.getStyleClass().add("prenotazione-slider");

        Label[] labelGeneriche = {titoloOrari, labelNome, labelData, labelTipo, labelPosto, labelOpzioni, labelCosto};
        for (Label l : labelGeneriche) l.getStyleClass().add("prenotazione-label");
        GUI_Prenotazione.labelCosto.getStyleClass().add("prenotazione-costo-label");

        comboTipo.getStyleClass().add("prenotazione-combotipo");
        dataPicker.getStyleClass().add("prenotazione-datapicker");

        checkBoxOpzioni.getStyleClass().add("prenotazione-checkbox");
        boxOpzioniExtra.getStyleClass().add("box-opzioni-extra");
        for (CheckBox cb : opzioni) cb.getStyleClass().add("prenotazione-checkbox");
    }


    private static Button creaBottoneScegliPosto(DatePicker dataPicker, Utente utente, TextField campoNome, TextField campoCognome, TextField campoTarga, ComboBox<String> comboTipo, CheckBox checkBoxOpzioni, CheckBox[] opzioni) {
        Button scegliPosto = new Button("Controlla Disponibilità Posto");
        scegliPosto.getStyleClass().add("pulsante-controlla");
        scegliPosto.setDisable(true);
        scegliPosto.setOnAction(_ -> {
            LocalDate data = dataPicker.getValue();
            double arrivoTemp = sliderArrivo.getValue();
            double partenzaTemp = sliderPartenza.getValue();
            boolean usaOpzioni = checkBoxOpzioni.isSelected();
            List<String> opzioniSelezionate = new ArrayList<>();
            for (CheckBox cb : opzioni)
                if (cb.isSelected()) opzioniSelezionate.add(cb.getText());
            DatiPrenotazioneTemp temp = new DatiPrenotazioneTemp(
                    campoNome.getText(), campoCognome.getText(), campoTarga.getText(), comboTipo.getValue(),
                    data, arrivoTemp, partenzaTemp, xPosto, yPosto,
                    usaOpzioni, opzioniSelezionate
            );
            aggiornaStatoBottoneScegliPosto(scegliPosto, dataPicker, sliderArrivo, sliderPartenza);
            temp.usaOpzioni = checkBoxOpzioni.isSelected();
            for (CheckBox cb : opzioni)
                if (cb.isSelected()) temp.opzioniSelezionate.add(cb.getText());

            LocalDateTime arrivo = InterfacciaHelper.creaDataOrario(data, sliderArrivo);
            LocalDateTime partenza = InterfacciaHelper.creaDataOrario(data, sliderPartenza);
            GUI_VisualizzaParcheggio.scegliPosto(arrivo, partenza, utente, temp, postoSelezionato -> {
                temp.XPosto = postoSelezionato[0];
                temp.YPosto = postoSelezionato[1];
                xPosto = postoSelezionato[0];
                yPosto = postoSelezionato[1];
                GUI_Prenotazione.mostraFinestraPrenotazione(utente, temp);
            });
        });
        return scegliPosto;
    }

    private static void confermaPrenotazione(Utente utente, String nome, String cognome, String targa, String tipo, double costo, String dataOrarioArrivo, String dataOrarioPartenza, int numeroPosto, boolean opzioniSelezionate, CheckBox[] opzioni, Runnable azioneConfermata) {
        StackPane overlayPane = new StackPane();
        overlayPane.getStylesheets().add(Objects.requireNonNull(GUI_Prenotazione.class.getResource("/risorse/css/confermaPrenotazione.css")).toExternalForm());

        Rectangle sfondoOscurato = new Rectangle();
        sfondoOscurato.setFill(Color.rgb(0, 0, 0, 0.6));
        sfondoOscurato.setEffect(new GaussianBlur(15));
        sfondoOscurato.widthProperty().bind(root.widthProperty());
        sfondoOscurato.heightProperty().bind(root.heightProperty());

        VBox finestra = new VBox(10);
        finestra.getStyleClass().add("finestra-conferma");
        finestra.setPadding(new Insets(20));
        finestra.setAlignment(Pos.CENTER);
        finestra.setMaxWidth(400);
        finestra.setEffect(new DropShadow(20, Color.BLACK));

        String riepilogo = String.format(
                "Nome: %s %s\nTarga: %s\nTipo veicolo: %s\nData/Orario arrivo: %s\nData/Orario partenza: %s\nNumero Posto: %s\nCosto totale: %.2f €",
                nome, cognome, targa, tipo, dataOrarioArrivo, dataOrarioPartenza, numeroPosto, costo
        );
        Label titolo = new Label("Conferma i seguenti dati:");
        titolo.getStyleClass().add("titolo-popup");
        Label dettagli = new Label(riepilogo);
        dettagli.getStyleClass().add("riepilogo-contenuto");
        dettagli.setWrapText(true);
        HBox pulsanti = new HBox(25);
        pulsanti.setAlignment(Pos.CENTER);
        Button conferma = new Button("Conferma");
        Button modifica = new Button("Modifica");
        conferma.getStyleClass().add("bottone-popup");
        modifica.getStyleClass().add("bottone-popup");
        pulsanti.getChildren().addAll(conferma, modifica);

        conferma.setOnAction(_ -> {
            root.getChildren().remove(overlayPane);
            azioneConfermata.run();
            InterfacciaHelper.mostraConferma("Prenotazione effettuata con successo!");
        });
        modifica.setOnAction(_ -> {
            root.getChildren().remove(overlayPane);
            LocalDateTime arrivo = LocalDateTime.parse(dataOrarioArrivo, DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm"));
            LocalDateTime partenza = LocalDateTime.parse(dataOrarioPartenza, DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm"));
            Posto postoTemporaneo = new Posto(utente, nome, cognome, targa, tipo, costo, arrivo, partenza, (numeroPosto - 1) / 5, (numeroPosto - 1) % 5);
            ModificaPrenotazione.mostraFinestra(utente, postoTemporaneo, opzioniSelezionate, opzioni, postoAggiornato -> {
                GestionePrenotazioni.prenota(postoAggiornato.utente(), postoAggiornato.nome(), postoAggiornato.cognome(), postoAggiornato.targa(), postoAggiornato.tipo(), InterfacciaHelper.creaDatePicker(), InterfacciaHelper.creaSliderOrario(postoAggiornato.dataArrivo()), InterfacciaHelper.creaSliderOrario(postoAggiornato.dataPartenza()), postoAggiornato.costo(), postoAggiornato.x(), postoAggiornato.y());
                InterfacciaHelper.mostraConferma("Prenotazione modificata! Nuovo costo: " + postoAggiornato.costo() + " euro");
            });
        });
        finestra.getChildren().addAll(titolo, dettagli, pulsanti);
        StackPane.setAlignment(sfondoOscurato, Pos.CENTER);
        StackPane.setAlignment(finestra, Pos.CENTER);
        overlayPane.getChildren().addAll(sfondoOscurato, finestra);
        root.getChildren().add(overlayPane);
    }

    private static Button creaBottonePrenota(Utente utente, TextField campoNome, TextField campoCognome,
                                             TextField campoTarga, ComboBox<String> comboTipo,
                                             DatePicker dataPicker, CheckBox checkBoxOpzioni,
                                             CheckBox[] opzioni) {
        // Riepilogo prima di uscire
        return InterfacciaHelper.creaPulsante("Prenota", _ -> {
            if (InterfacciaHelper.controllaErrori(campoNome, campoCognome, campoTarga, comboTipo, dataPicker, sliderArrivo, sliderPartenza, checkBoxOpzioni, opzioni, xPosto, yPosto)) {
                double costo = GestionePrenotazioni.calcolaCosto(comboTipo.getValue(), dataPicker.getValue(), sliderArrivo, sliderPartenza, checkBoxOpzioni.isSelected(), opzioni);

                // Riepilogo prima di uscire
                String nome = campoNome.getText();
                String cognome = campoCognome.getText();
                String targa = campoTarga.getText();
                String tipo = comboTipo.getValue();
                String dataOrarioArrivo = InterfacciaHelper.creaStringOrario(dataPicker.getValue(), sliderArrivo);
                String dataOrarioPartenza = InterfacciaHelper.creaStringOrario(dataPicker.getValue(), sliderPartenza);
                int numPosto = (xPosto * 5) + yPosto + 1;
                confermaPrenotazione(utente, nome, cognome, targa, tipo, costo, dataOrarioArrivo, dataOrarioPartenza, numPosto, checkBoxOpzioni.isSelected(), opzioni, () -> {
                    GestionePrenotazioni.prenota(utente, nome, cognome, targa, tipo, dataPicker, sliderArrivo, sliderPartenza, costo, xPosto, yPosto);
                    InterfacciaHelper.mostraConferma("Prenotazione completata! Costo finale: " + costo + " euro");
                    labelCosto.setText("Costo della prenotazione: " + costo + " euro");
                    SchermataIniziale.aggiornaMenu(utente);
                });
            }
        });
    }

    private static void impostaListener(ComboBox<String> comboTipo, DatePicker dataPicker,
                                        CheckBox checkBoxOpzioni, CheckBox[] opzioni, Button scegliPosto,
                                        Label orarioArrivo, Label orarioPartenza) {
        Runnable aggiornaCosto = () -> {
            String tipo = comboTipo.getValue();
            LocalDate data = dataPicker.getValue();
            if (tipo == null) tipo = "default";
            if (data == null) data = LocalDate.now();
            double costo = GestionePrenotazioni.calcolaCosto(tipo, data, sliderArrivo, sliderPartenza, checkBoxOpzioni.isSelected(), opzioni);
            labelCosto.setText("Costo della prenotazione: " + costo + " euro");
        };
        comboTipo.setOnAction(_ -> aggiornaCosto.run());
        for (CheckBox cb : opzioni)
            cb.setOnAction(_ -> aggiornaCosto.run());
        dataPicker.setOnAction(_ -> {
            aggiornaCosto.run();
            aggiornaStatoBottoneScegliPosto(scegliPosto, dataPicker, sliderArrivo, sliderPartenza);
        });
        sliderArrivo.valueProperty().
                addListener((_, _, newVal) -> {
                    orarioArrivo.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue()));
                    aggiornaCosto.run();
                    aggiornaStatoBottoneScegliPosto(scegliPosto, dataPicker, sliderArrivo, sliderPartenza);
                });
        sliderPartenza.valueProperty().
                addListener((_, _, newVal) -> {
                    orarioPartenza.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue()));
                    aggiornaCosto.run();
                    aggiornaStatoBottoneScegliPosto(scegliPosto, dataPicker, sliderArrivo, sliderPartenza);
                });
    }

    private static void aggiornaStatoBottoneScegliPosto(Button scegliPosto, DatePicker dataPicker, Slider sliderArrivo, Slider sliderPartenza) {
        LocalDate data = dataPicker.getValue();
        boolean dataOk = data != null && !data.isBefore(LocalDate.now());
        boolean orariOk = sliderArrivo.getValue() < sliderPartenza.getValue();
        scegliPosto.setDisable(!(dataOk && orariOk));
    }

    private static VBox creaTabellaPrezzi() {
        VBox tabellaPrezziBox = new VBox(5);
        tabellaPrezziBox.setPadding(new Insets(20));
        tabellaPrezziBox.setPrefWidth(500);
        GridPane listinoPrezzi = ListinoPrezzi.creaListinoPrezzi();
        listinoPrezzi.setAlignment(Pos.CENTER_RIGHT);
        tabellaPrezziBox.getChildren().addAll(listinoPrezzi);
        return tabellaPrezziBox;
    }

    private static void impostaMargini(TextField campoNome, TextField campoCognome, TextField campoTarga,
                                       ComboBox<String> comboTipo, DatePicker dataPicker, HBox boxOrari,
                                       CheckBox checkBoxOpzioni, Button scegliPosto, VBox boxOpzioniExtra, HBox pulsanti) {
        impostaMarginiVert(campoNome, 10, 0);
        impostaMarginiVert(campoCognome, 0, 10);
        impostaMarginiVert(dataPicker, 0, 10);
        impostaMarginiVert(boxOrari, 15, 15);
        impostaMarginiVert(comboTipo, 10, 0);
        impostaMarginiVert(campoTarga, 0, 10);
        impostaMarginiVert(scegliPosto, 10, 25);
        impostaMarginiVert(checkBoxOpzioni, 10, 25);
        impostaMarginiVert(boxOpzioniExtra, 0, 25);
        impostaMarginiVert(labelCosto, 0, 10);
        impostaMarginiVert(pulsanti, 20, 0);
    }

    private static void impostaMarginiVert(Node nodo, double top, double bottom) {
        VBox.setMargin(nodo, new Insets(top, 0, bottom, 0));
    }

    private static void riempiCampiTemp(DatiPrenotazioneTemp temp, TextField campoNome, TextField campoCognome,
                                        TextField campoTarga, ComboBox<String> comboTipo, DatePicker dataPicker,
                                        CheckBox checkBoxOpzioni, CheckBox[] opzioni, VBox boxOpzioniExtra, Button scegliPosto) {
        if (temp == null) return;
        campoNome.setText(temp.nome);
        campoCognome.setText(temp.cognome);
        campoTarga.setText(temp.targa);
        comboTipo.setValue(temp.tipo);
        dataPicker.setValue(temp.data);
        sliderArrivo.setValue(temp.orarioArrivo);
        sliderPartenza.setValue(temp.orarioPartenza);
        xPosto = temp.XPosto;
        yPosto = temp.YPosto;
        checkBoxOpzioni.setSelected(temp.usaOpzioni);
        if (temp.opzioniSelezionate != null)
            for (CheckBox cb : opzioni)
                cb.setSelected(temp.opzioniSelezionate.contains(cb.getText()));
        InterfacciaHelper.toggleBox(boxOpzioniExtra, checkBoxOpzioni.isSelected());
        aggiornaStatoBottoneScegliPosto(scegliPosto, dataPicker, sliderArrivo, sliderPartenza);
        aggiornaLabelCosto(temp.tipo, temp.data, sliderArrivo, sliderPartenza, temp.usaOpzioni, opzioni);
    }

    private static void aggiornaLabelCosto(String tipo, LocalDate data, Slider orarioArrivo, Slider orarioPartenza, boolean usaExtra, CheckBox[] opzioniExtra) {
        double costo = GestionePrenotazioni.calcolaCosto(tipo, data, orarioArrivo, orarioPartenza, usaExtra, opzioniExtra);
        labelCosto.setText("Costo della prenotazione: " + costo + " euro");
    }
}