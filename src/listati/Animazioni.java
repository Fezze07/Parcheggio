package listati;

import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class Animazioni {
    private static BorderPane base;
    public static void setBase(BorderPane root) {
        base = root;
    }
    public static void creaLogo(ImageView logo) {
        logo.setFitHeight(150);
        logo.setPreserveRatio(true);
        // ANIMAZIONE: fade + slide da sinistra
        logo.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), logo);
        slide.setFromX(-300);
        slide.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), logo);
        fade.setFromValue(0);
        fade.setToValue(1);
        ParallelTransition animazioneLogo = new ParallelTransition(slide, fade);
        animazioneLogo.play();
    }

    public static void creaData(Label orologio, Label data) {
        orologio.getStyleClass().add("orologio");
        data.getStyleClass().add("data");
        DateTimeFormatter formatoOra = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy");
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, _ -> {
                    orologio.setText(LocalTime.now().format(formatoOra));
                    data.setText(LocalDate.now().format(formatoData));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    public static void mostraInfoChiSiamo() {
        Stage popUpStage = new Stage();
        popUpStage.initOwner(base.getScene().getWindow());
        popUpStage.setTitle("Chi siamo");

        // Aggiunta dell'immagine
        ImageView immagine = new ImageView(new Image(Objects.requireNonNull(Animazioni.class.getResourceAsStream("/risorse/immagini/foto_fezze.jpg"))));
        immagine.setFitWidth(450);
        immagine.setFitHeight(550);

        Text parte1 = new Text("Applicazione sviluppata da ");
        parte1.getStyleClass().add("popupAnimazioni-text");
        Text nome = new Text("Federico Cisera");
        Text parte2 = new Text(".\nGestione parcheggio smart, semplice e veloce!\nSupporto al cliente sempre disponibile!\n\nContatti: ");
        parte2.getStyleClass().add("popupAnimazioni-text");
        Text mail = new Text("ciserafezze@gmail.com");
        parte1.setFill(Color.WHITE);
        parte2.setFill(Color.WHITE);
        nome.setFill(Color.web("#00BCD4"));     nome.getStyleClass().add("popupAnimazioni-text-evidenziato");
        mail.setFill(Color.web("#00BCD4"));     mail.getStyleClass().add("popupAnimazioni-text-evidenziato");
        TextFlow descrizione = new TextFlow(parte1, nome, parte2, mail);
        descrizione.getStyleClass().add("popup-text");
        descrizione.setTextAlignment(TextAlignment.CENTER);
        Button chiudi = InterfacciaHelper.creaPulsante("Chiudi", _ -> popUpStage.close());

        VBox content = new VBox();
        content.setSpacing(10);
        content.getStyleClass().setAll("popupAnimazioni");
        content.getChildren().addAll(immagine, descrizione, chiudi);
        Scene popUpScene = new Scene(content, 650, 850);
        popUpScene.getStylesheets().add(Objects.requireNonNull(Animazioni.class.getResource("/risorse/css/animazioni.css")).toExternalForm());
        popUpStage.setScene(popUpScene);
        popUpStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.5), content);
        scaleUp.setFromX(0.8);
        scaleUp.setFromY(0.8);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        fadeIn.play();
        scaleUp.play();
    }
}
