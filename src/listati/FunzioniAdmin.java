package listati;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class FunzioniAdmin {
    private static BorderPane base;

    public static void setBase(BorderPane root) {
        base = root;
    }

    public static void visualizzaPrenotazioniAttive(Utente utente, Connection conn) {
        eliminaPrenotazioniVecchie(conn);
        List<Posto> prenotazioni = new ArrayList<>();
        String query = "SELECT * FROM prenotazioni WHERE data_partenza >= CURDATE() ORDER BY data_arrivo, ora_arrivo";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Utente u = new Utente(rs.getString("nome_utente"), "", "");
                    LocalDateTime dataArrivo = LocalDateTime.of(rs.getDate("data_arrivo").toLocalDate(), rs.getTime("ora_arrivo").toLocalTime());
                    LocalDateTime dataPartenza = LocalDateTime.of(rs.getDate("data_partenza").toLocalDate(), rs.getTime("ora_partenza").toLocalTime());
                    Posto p = new Posto(u,
                            rs.getString("nome"),
                            rs.getString("cognome"),
                            rs.getString("targa"),
                            rs.getString("tipo"),
                            rs.getDouble("costo"),
                            dataArrivo,
                            dataPartenza,
                            rs.getInt("xPosto"),
                            rs.getInt("yPosto")
                    );
                    prenotazioni.add(p);
                }
              }
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database: " + e.getMessage());
            return;
        }
        if (prenotazioni.isEmpty()) {
            InterfacciaHelper.mostraErrore("Non ci sono prenotazioni attive!");
            return;
        }
        // Visualizzare le prenotazioni
        ComboBox<Posto> comboPrenotazioni = new ComboBox<>();
        comboPrenotazioni.getItems().addAll(prenotazioni);
        comboPrenotazioni.setPromptText("Prenotazioni Attive");
        comboPrenotazioni.getStyleClass().add("combo-selezione");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        comboPrenotazioni.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Posto p, boolean vuoto) {
                super.updateItem(p, vuoto);
                if (vuoto || p == null) {
                    setText(null);
                } else {
                    setText("📅 " + p.dataArrivo().format(formatter) + " - " + p.dataPartenza().format(formatter) +
                            " | 🚗 " + p.targa() +
                            " | Tipo: " + p.tipo() +
                            " | 💸 €" + p.costo());
                }
            }
        });
        comboPrenotazioni.setButtonCell(comboPrenotazioni.getCellFactory().call(null));
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        Button cancella = InterfacciaHelper.creaPulsante("Cancella prenotazione", _ -> eliminaPrenotazioneAdmin(utente, conn));

        VBox layout = new VBox(10, comboPrenotazioni, cancella, esci);
        layout.getStyleClass().add("box-cancellazione");
        layout.setAlignment(Pos.CENTER);
        base.setCenter(layout);
    }

    public static void eliminaPrenotazioneUtente(Utente utente, Connection conn) {
        String query = "SELECT * FROM prenotazioni WHERE nome_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, utente.getNomeUtente());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Posto> lista = new ArrayList<>();
                while (rs.next()) {
                    Posto p = Posto.fromResultSet(rs);
                    lista.add(p);
                }
                mostraInterfacciaEliminazione(utente, lista, false, conn);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento prenotazioni utente: " + e.getMessage());
        }
    }

    public static void eliminaPrenotazioneAdmin(Utente admin, Connection conn) {
        String query = "SELECT * FROM prenotazioni";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            List<Posto> lista = new ArrayList<>();
            while (rs.next()) {
                Posto p = Posto.fromResultSet(rs);
                lista.add(p);
            }
            mostraInterfacciaEliminazione(admin, lista, true, conn);
        } catch (SQLException e) {
            System.err.println("Errore durante il caricamento prenotazioni: " + e.getMessage());
        }
    }

    public static void eliminaPrenotazioniVecchie(Connection conn) {
        String deleteSQL = "DELETE FROM prenotazioni WHERE (data_partenza < CURDATE()) OR (data_partenza = CURDATE() AND ora_partenza < CURTIME())";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante eliminazione prenotazioni vecchie: " + e.getMessage());
        }
    }

    private static void mostraInterfacciaEliminazione(Utente utente, List<Posto> lista, boolean mostraUtente, Connection conn) {
        ComboBox<Posto> scelta = new ComboBox<>();
        scelta.getItems().addAll(lista);
        scelta.setPromptText("Seleziona una prenotazione da cancellare");
        scelta.getStyleClass().add("combo-selezione");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        scelta.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Posto p, boolean vuoto) {
                super.updateItem(p, vuoto);
                if (vuoto || p == null) setText(null);
                else {
                    String info = (mostraUtente ? "Utente: " + p.utente().getNomeUtente() + " | " : "") +
                            "Posto " + p.x() + "," + p.y() +
                            " | 📅 " + p.dataArrivo().format(formatter) + " - " + p.dataPartenza().format(formatter) +
                            " | 🚗 " + p.targa() +
                            " | Tipo: " + p.tipo() +
                            " | 💸 €" + p.costo();
                    setText(info);
                }
            }
        });
        scelta.setButtonCell(scelta.getCellFactory().call(null));
        Button conferma = InterfacciaHelper.creaPulsante("Conferma cancellazione", _ -> {
            Posto selezionato = scelta.getValue();
            if (selezionato != null) {
                String deleteSQL = """
                DELETE FROM prenotazioni
                WHERE nome_utente = ? AND xPosto = ? AND yPosto = ? AND data_arrivo = ? AND data_partenza = ? AND targa = ?
            """;
                try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                    stmt.setString(1, selezionato.utente().getNomeUtente());
                    stmt.setInt(2, selezionato.x());
                    stmt.setInt(3, selezionato.y());
                    stmt.setTimestamp(4, java.sql.Timestamp.valueOf(selezionato.dataArrivo()));
                    stmt.setTimestamp(5, java.sql.Timestamp.valueOf(selezionato.dataPartenza()));
                    stmt.setString(6, selezionato.targa());
                    int righe = stmt.executeUpdate();
                    if (righe > 0) {
                        InterfacciaHelper.mostraConferma("Prenotazione eliminata con successo.");
                        if (mostraUtente) eliminaPrenotazioneAdmin(utente, conn);
                        else eliminaPrenotazioneUtente(utente, conn);
                    } else {
                        InterfacciaHelper.mostraErrore("Nessuna prenotazione trovata da eliminare.");
                    }
                } catch (SQLException e) {
                    System.err.println("Errore durante l'eliminazione: " + e.getMessage());
                }
            } else {
                InterfacciaHelper.mostraErrore("Seleziona una prenotazione prima di confermare.");
            }
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        HBox pulsantiBox = new HBox(40, conferma, esci);
        pulsantiBox.setAlignment(Pos.CENTER);
        VBox layout = new VBox(10, scelta, pulsantiBox);
        layout.getStyleClass().add("box-cancellazione");
        layout.setAlignment(Pos.CENTER);
        base.setCenter(layout);
    }

    public static void cancellaUtente(Utente admin, Connection conn) {
        ComboBox<Utente> scelta = new ComboBox<>();
        scelta.setPromptText("Seleziona un utente da eliminare");
        scelta.getStyleClass().add("combo-selezione");

        String query = "SELECT nome_utente, password, tipo FROM utenti WHERE tipo != 'Admin'";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String nome = rs.getString("nome_utente");
                String password = rs.getString("password");
                String tipo = rs.getString("tipo");
                Utente utente = new Utente(nome, password, tipo);
                scelta.getItems().add(utente);
            }
        } catch (SQLException e) {
            System.err.println("Errore caricamento utenti: " + e.getMessage());
            return;
        }

        scelta.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Utente u, boolean vuoto) {
                super.updateItem(u, vuoto);
                if (vuoto || u == null) setText(null);
                else setText("👤 " + u.nomeUtente + " | 🔒 " + u.password);
            }
        });
        scelta.setButtonCell(scelta.getCellFactory().call(null));
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(admin));
        Button conferma = InterfacciaHelper.creaPulsante("Conferma Eliminazione", _ -> {
            Utente selezionato = scelta.getValue();
            if (selezionato != null) {
                GestioneUtenti.cancellaUtente(selezionato);
                InterfacciaHelper.mostraConferma("Utente eliminato con successo.");
            } else {
                InterfacciaHelper.mostraErrore("Seleziona un utente prima.");
            }
        });
        VBox layout = new VBox(10, scelta, conferma, esci);
        layout.getStyleClass().add("box-cancellazione");
        layout.setAlignment(Pos.CENTER);
        base.setCenter(layout);
    }

    public static void modificaTariffe(Utente utente) {
        // ====== SEZIONE VEICOLI ======
        TitledPane sezioneVeicoli = creaSezione(
                "Prezzi Veicoli",
                Prezzi::getPrezzo,
                Prezzi::setPrezzoVeicolo,
                Map.of(
                        "Auto", "Auto",
                        "Moto", "Moto",
                        "Camion", "Camion",
                        "Corriera", "Corriera"
                )
        );
        sezioneVeicoli.getStyleClass().add("sezione-veicoli");
        // ====== SEZIONE OPZIONI EXTRA ======
        TitledPane sezioneOpzioni = creaSezione(
                "Prezzi Opzioni Extra",
                Prezzi::getPrezzoOpzioni,
                Prezzi::setPrezzoOpzione,
                Map.of(
                        1, "Custodia Parcheggio",
                        2, "Albergo",
                        3, "Accesso a disabili",
                        4, "Bagni",
                        5, "Area Picnic",
                        6, "Pieno carburante"
                )
        );
        sezioneOpzioni.getStyleClass().add("sezione-opzioni-extra");
        // ====== SEZIONE GIORNI ======
        Map<String, String> giorniOrdinati = new LinkedHashMap<>();
        giorniOrdinati.put("MONDAY", "Lunedì");
        giorniOrdinati.put("TUESDAY", "Martedì");
        giorniOrdinati.put("WEDNESDAY", "Mercoledì");
        giorniOrdinati.put("THURSDAY", "Giovedì");
        giorniOrdinati.put("FRIDAY", "Venerdì");
        giorniOrdinati.put("SATURDAY", "Sabato");
        giorniOrdinati.put("SUNDAY", "Domenica");
        TitledPane sezioneGiorni = creaSezione(
                "Prezzi Giornalieri",
                g -> Prezzi.getPrezzoGiorno(DayOfWeek.valueOf(g)),
                (g, p) -> Prezzi.setPrezzoGiorno(DayOfWeek.valueOf(g), p),
                giorniOrdinati
        );
        sezioneGiorni.getStyleClass().add("sezione-giorni");
        // ====== SEZIONE COSTO ORARIO ======
        Label labelOrario = new Label("Costo ogni 15 minuti:");
        TextField campoOrario = new TextField(String.valueOf(Prezzi.costoOrario));
        labelOrario.getStyleClass().add("etichetta-orario");
        campoOrario.getStyleClass().add("campo-costo-orario");

        HBox orarioBox = new HBox(10, labelOrario, campoOrario);
        orarioBox.setAlignment(Pos.CENTER);
        orarioBox.getStyleClass().add("contenitore-orario");

        // ====== PULSANTI ======
        Button salva = InterfacciaHelper.creaPulsante("Salva Tutto", _ -> {
            try {
                Prezzi.setCostoOrario(Double.parseDouble(campoOrario.getText()));
                SalvaCarica.esportaPrezzi();
                InterfacciaHelper.mostraConferma("Tutte le tariffe sono state aggiornate con successo!");
            } catch (NumberFormatException e) {
                InterfacciaHelper.mostraErrore("Il costo orario deve essere un numero valido.");
            }
        });

        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        VBox contenitoreTariffe = new VBox(20);
        contenitoreTariffe.setAlignment(Pos.CENTER);
        contenitoreTariffe.getStyleClass().add("contenitore-tariffe");
        contenitoreTariffe.getChildren().addAll(sezioneVeicoli, sezioneOpzioni, sezioneGiorni, orarioBox, salva, esci);

        VBox.setMargin(sezioneVeicoli, new Insets(0, 0, 0, 0));
        sezioneVeicoli.setMaxWidth(450);
        sezioneVeicoli.setPrefWidth(450);
        sezioneOpzioni.setMaxWidth(450);
        sezioneOpzioni.setPrefWidth(450);
        sezioneGiorni.setMaxWidth(450);
        sezioneGiorni.setPrefWidth(450);

        base.setCenter(contenitoreTariffe);
    }

    private static <T> TitledPane creaSezione(String titolo, Function<T, Integer> getter, BiConsumer<T, Integer> setter, Map<T, String> etichette) {
        GridPane grigliaSezione = new GridPane();
        grigliaSezione.setHgap(15);
        grigliaSezione.setVgap(12);
        grigliaSezione.getStyleClass().add("griglia-sezione");
        int riga = 0;
        for (T chiave : etichette.keySet()) {
            Label etichettaPrezzo = new Label(etichette.get(chiave) + ":");
            etichettaPrezzo.getStyleClass().add("etichetta-selezione");
            TextField campoPrezzo = new TextField(String.valueOf(getter.apply(chiave)));
            campoPrezzo.getStyleClass().add("campo-selezione");

            grigliaSezione.add(etichettaPrezzo, 0, riga);
            grigliaSezione.add(campoPrezzo, 1, riga);
            campoPrezzo.textProperty().addListener((_, _, newVal) -> {
                try {
                    int valore = Integer.parseInt(newVal);
                    setter.accept(chiave, valore);
                } catch (NumberFormatException e) {
                    InterfacciaHelper.mostraErrore("Inserisci un formato valido!");
                }
            });
            riga++;
        }
        TitledPane pane = new TitledPane(titolo, grigliaSezione);
        pane.getStyleClass().add("pannello");
        pane.setExpanded(false);
        return pane;
    }

    public static void modificaApertura(Utente utente) {
        TextField campoApertura = InterfacciaHelper.creaCampoTesto(String.valueOf(InterfacciaHelper.getApertura()));
        TextField campoChiusura = InterfacciaHelper.creaCampoTesto(String.valueOf(InterfacciaHelper.getChiusura()));
        campoApertura.getStyleClass().add("campo-selezione");
        campoChiusura.getStyleClass().add("campo-selezione");

        VBox contenutoOrari = new VBox(10);
        contenutoOrari.setPadding(new Insets(10));
        Label labelApertura = new Label("Orario di apertura:");
        labelApertura.getStyleClass().add("etichetta-selezione");
        Label labelChiusura = new Label("Orario di chiusura:");
        labelChiusura.getStyleClass().add("etichetta-selezione");
        contenutoOrari.getChildren().addAll(labelApertura, campoApertura, labelChiusura, campoChiusura);
        contenutoOrari.getStyleClass().add("griglia-sezione");

        TitledPane orari = new TitledPane("Orari Apertura", contenutoOrari);
        orari.getStyleClass().add("pannello");
        orari.getStyleClass().add("sezione-orari");
        orari.setExpanded(true);
        orari.setMaxWidth(450);
        orari.setPrefWidth(450);

        Button salva = InterfacciaHelper.creaPulsante("Salva Tutto", _ -> {
            try {
                String aperturaText = campoApertura.getText().trim().replace(",", ".");
                String chiusuraText = campoChiusura.getText().trim().replace(",", ".");
                Double oraApertura = aperturaText.isEmpty() ? null : Double.parseDouble(aperturaText);
                Double oraChiusura = chiusuraText.isEmpty() ? null : Double.parseDouble(chiusuraText);
                if (oraApertura != null) {
                    if (oraApertura < 0 || oraApertura > 23) {
                        InterfacciaHelper.mostraErrore("L'orario di apertura deve essere tra 0 e 23.");
                        return;
                    }
                    if (InterfacciaHelper.multiploDiQuindici(oraApertura)) {
                        InterfacciaHelper.mostraErrore("L'orario di apertura deve essere multiplo di 15 minuti (es. 8.15, 8.30, 8.45).");
                        return;
                    }
                }
                if (oraChiusura != null) {
                    if (oraChiusura < 0 || oraChiusura > 23) {
                        InterfacciaHelper.mostraErrore("L'orario di chiusura deve essere tra 0 e 23.");
                        return;
                    }
                    if (InterfacciaHelper.multiploDiQuindici(oraChiusura)) {
                        InterfacciaHelper.mostraErrore("L'orario di chiusura deve essere multiplo di 15 minuti (es. 8.15, 8.30, 8.45).");
                        return;
                    }
                }
                if (oraApertura != null && oraChiusura != null && oraApertura >= oraChiusura) {
                    InterfacciaHelper.mostraErrore("L'orario di apertura deve essere prima di quello di chiusura.");
                    return;
                }
                InterfacciaHelper.setOrariParcheggi(oraApertura, oraChiusura);
                InterfacciaHelper.mostraConferma("Orari aggiornati con successo!");
            } catch (NumberFormatException e) {
                InterfacciaHelper.mostraErrore("Inserisci un numero valido! Usa il punto o la virgola per i decimali. (es. 8.30)");
            }
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        VBox contenitore = new VBox(20);
        contenitore.setAlignment(Pos.CENTER);
        contenitore.getChildren().addAll(orari, salva, esci);
        base.setCenter(contenitore);
    }

    public static void inserisciDateChiusura(Utente utente) {
        VBox contenitore = new VBox(15);
        contenitore.setPadding(new Insets(15));
        contenitore.setAlignment(Pos.CENTER);
        Label titolo = new Label("Inserisci date di chiusura");
        titolo.getStyleClass().add("etichetta-selezione");

        HBox inputBox = new HBox(8);
        TextField giorno = InterfacciaHelper.creaCampoTesto("");
        giorno.setPromptText("GG");
        giorno.setPrefWidth(50);
        giorno.getStyleClass().add("campo-selezione");
        TextField mese = InterfacciaHelper.creaCampoTesto("");
        mese.setPromptText("MM");
        mese.setPrefWidth(50);
        mese.getStyleClass().add("campo-selezione");
        TextField anno = InterfacciaHelper.creaCampoTesto("");
        anno.setPromptText("AAAA");
        anno.setPrefWidth(70);
        anno.getStyleClass().add("campo-selezione");

        Button aggiungi = new Button("Aggiungi");
        aggiungi.getStyleClass().add("buttone-aggiungi-date");
        inputBox.getChildren().addAll(giorno, mese, anno, aggiungi);
        inputBox.setAlignment(Pos.CENTER);
        VBox listaDate = new VBox(6);
        listaDate.setVisible(false);
        listaDate.getStyleClass().add("lista-date");
        listaDate.setAlignment(Pos.CENTER);
        listaDate.setMaxWidth(400);
        listaDate.setPadding(new Insets(10));
        Set<LocalDate> dateInserite = new HashSet<>();

        Runnable aggiornaLista = () -> {
            listaDate.setVisible(true);
            listaDate.getChildren().clear();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (LocalDate d : dateInserite) {
                String dataFormattata = d.format(formatter);
                Label lbl = new Label(dataFormattata);
                lbl.getStyleClass().add("etichetta-selezione");
                listaDate.getChildren().add(lbl);
            }
        };
        aggiungi.setOnAction(_ -> {
            try {
                int g = Integer.parseInt(giorno.getText().trim());
                int m = Integer.parseInt(mese.getText().trim());
                int a = Integer.parseInt(anno.getText().trim());
                LocalDate data = LocalDate.of(a, m, g);
                if (dateInserite.contains(data)) {
                    InterfacciaHelper.mostraErrore("Data già inserita!");
                    return;
                }
                dateInserite.add(data);
                aggiornaLista.run();
                giorno.clear();
                mese.clear();
                anno.clear();
            } catch (DateTimeException ex) {
                InterfacciaHelper.mostraErrore("Data non valida!");
            } catch (NumberFormatException ex) {
                InterfacciaHelper.mostraErrore("Inserisci numeri validi!");
            }
        });
        Button salva = InterfacciaHelper.creaPulsante("Salva date", _ -> {
            InterfacciaHelper.setGiorniChiusura(dateInserite);
            InterfacciaHelper.mostraConferma("Date salvate!");
        });
        Button esci = InterfacciaHelper.creaPulsante("Esci", _ -> SchermataIniziale.aggiornaMenu(utente));
        contenitore.getChildren().addAll(titolo, inputBox, listaDate, salva, esci);
        base.setCenter(contenitore);
    }

}
