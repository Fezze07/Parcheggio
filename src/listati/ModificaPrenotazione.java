package listati;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class ModificaPrenotazione {
    private static int xPosto;
    private static int yPosto;
    private static BorderPane base;

    public static void setBase(BorderPane root) {
        base = root;
    }

    public static void mostraFinestra(Utente utente, Posto prenotazioneEsistente, boolean opzioniUsate, CheckBox[] opzioni, Consumer<Posto> callbackAggiornamento) {
        Stage stage = new Stage();
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);

        TextField campoNome = InterfacciaHelper.creaCampoTesto(prenotazioneEsistente.nome());
        TextField campoCognome = InterfacciaHelper.creaCampoTesto(prenotazioneEsistente.cognome());
        TextField campoTarga = InterfacciaHelper.creaCampoTesto(prenotazioneEsistente.targa());
        ComboBox<String> comboTipo = InterfacciaHelper.creaComboTipo();
        comboTipo.setValue(prenotazioneEsistente.tipo());
        comboTipo.getStyleClass().add("prenotazione-combotipo");
        DatePicker dataPicker = InterfacciaHelper.creaDatePicker();
        dataPicker.getStyleClass().add("prenotazione-datapicker");

        Slider sliderArrivo = InterfacciaHelper.creaSliderOrario(prenotazioneEsistente.dataArrivo());
        sliderArrivo.getStyleClass().add("prenotazione-slider");
        sliderArrivo.setMaxWidth(350);
        Slider sliderPartenza = InterfacciaHelper.creaSliderOrario(prenotazioneEsistente.dataPartenza());
        sliderPartenza.getStyleClass().add("prenotazione-slider");
        sliderPartenza.setMaxWidth(350);

        Label labelArrivo = new Label(InterfacciaHelper.getOrarioFromSlider(sliderArrivo.getValue()));
        labelArrivo.getStyleClass().add("prenotazione-label-orario");
        Label labelPartenza = new Label(InterfacciaHelper.getOrarioFromSlider(sliderPartenza.getValue()));
        labelPartenza.getStyleClass().add("prenotazione-label-orario");
        sliderArrivo.valueProperty().addListener((_, _, newVal) -> labelArrivo.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue())));
        sliderPartenza.valueProperty().addListener((_, _, newVal) -> labelPartenza.setText(InterfacciaHelper.getOrarioFromSlider(newVal.doubleValue())));

        xPosto = prenotazioneEsistente.x();
        yPosto = prenotazioneEsistente.y();

        Button scegliPosto = new Button("Controlla Disponibilità Posto");
        scegliPosto.getStyleClass().add("pulsante-controlla");
        scegliPosto.setOnAction(_ -> {
            LocalDate data = dataPicker.getValue();
            LocalDateTime arrivo = InterfacciaHelper.creaDataOrario(data, sliderArrivo);
            LocalDateTime partenza = InterfacciaHelper.creaDataOrario(data, sliderPartenza);
            GUI_VisualizzaParcheggio.scegliPosto(arrivo, partenza, utente, null, postoSelezionato -> {
                xPosto = postoSelezionato[0];
                yPosto = postoSelezionato[1];
            });
        });

        Button conferma = InterfacciaHelper.creaPulsante("Conferma Modifiche", _ -> {
            if (!InterfacciaHelper.controllaErrori(campoNome, campoCognome, campoTarga, comboTipo, dataPicker, sliderArrivo, sliderPartenza,
                    new CheckBox(), new CheckBox[0], prenotazioneEsistente.x(), prenotazioneEsistente.y())) return;
            LocalDate data = dataPicker.getValue();
            LocalTime oraArrivo = InterfacciaHelper.minutiToOrario((int) sliderArrivo.getValue());
            LocalTime oraPartenza = InterfacciaHelper.minutiToOrario((int) sliderPartenza.getValue());
            LocalDateTime arrivo = LocalDateTime.of(data, oraArrivo);
            LocalDateTime partenza = LocalDateTime.of(data, oraPartenza);
            double costo = Prezzi.calcolaTotale(
                    comboTipo.getValue(),
                    data,
                    arrivo,
                    partenza,
                    opzioniUsate,
                    opzioni
            );
            Posto aggiornato = new Posto(
                    utente,
                    campoNome.getText(),
                    campoCognome.getText(),
                    campoTarga.getText(),
                    comboTipo.getValue(),
                    costo,
                    arrivo,
                    partenza,
                    xPosto,
                    yPosto
            );
            callbackAggiornamento.accept(aggiornato);
            stage.close();
        });
        layout.getChildren().addAll(
                InterfacciaHelper.creaLabel("Nome"), campoNome,
                InterfacciaHelper.creaLabel("Cognome"), campoCognome,
                InterfacciaHelper.creaLabel("Targa"), campoTarga,
                InterfacciaHelper.creaLabel("Tipo Veicolo"), comboTipo,
                InterfacciaHelper.creaLabel("Data"), dataPicker,
                InterfacciaHelper.creaLabel("Orario Arrivo"), sliderArrivo, labelArrivo,
                InterfacciaHelper.creaLabel("Orario Partenza"), sliderPartenza, labelPartenza,
                scegliPosto,
                conferma
        );
        base.setCenter(layout);
    }
}