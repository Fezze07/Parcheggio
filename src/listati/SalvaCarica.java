package listati;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class SalvaCarica {
    public static void esportaParcheggio(Posto p) {
        try {
            String pathFile = GestoreDatabase.getPathDatabase("prenotazioni.txt");
            File file = new File(pathFile);
            boolean nuovoFile = !file.exists();
            try (FileWriter writer = new FileWriter(file, true)) {
                //Salvataggio locale SINGOLO prenotazione
                if (nuovoFile) {
                    writer.append("NomeUtente,Nome,Cognome,Targa,Tipo,Data Arrivo,Orario Arrivo,Data Partenza,Orario Partenza,Costo,x,y\n");
                }
                String riga = String.join(",",
                        p.utente().getNomeUtente(),
                        p.nome(),
                        p.cognome(),
                        p.targa(),
                        p.tipo(),
                        p.dataArrivo().toLocalDate().toString(),
                        p.dataArrivo().toLocalTime().toString(),
                        p.dataPartenza().toLocalDate().toString(),
                        p.dataPartenza().toLocalTime().toString(),
                        String.format(Locale.US, "%.2f", p.costo()),
                        String.valueOf(p.x()),
                        String.valueOf(p.y())
                );
                writer.append(riga).append("\n");
            }
            //Invia al Server TUTTI LE PRENOTAZIONI
            try (Connection conn = Connessione_SQL.creaConnessione()) {
                String query = """
                        INSERT INTO prenotazioni (nome_utente, nome, cognome, targa, tipo,
                        data_arrivo, ora_arrivo, data_partenza, ora_partenza,
                        costo, xPosto, yPosto)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, p.utente().getNomeUtente());
                    stmt.setString(2, p.nome());
                    stmt.setString(3, p.cognome());
                    stmt.setString(4, p.targa());
                    stmt.setString(5, p.tipo());
                    stmt.setDate(6, java.sql.Date.valueOf(p.dataArrivo().toLocalDate()));
                    stmt.setTime(7, java.sql.Time.valueOf(p.dataArrivo().toLocalTime()));
                    stmt.setDate(8, java.sql.Date.valueOf(p.dataPartenza().toLocalDate()));
                    stmt.setTime(9, java.sql.Time.valueOf(p.dataPartenza().toLocalTime()));
                    stmt.setDouble(10, p.costo());
                    stmt.setInt(11, p.x());
                    stmt.setInt(12, p.y());

                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore nella connessione al database:\n" + e.getMessage());
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Errore nell'esportazione locale:\n" + e.getMessage());
        }
    }

    public static void esportaUtenti(Utente utenteLoggato) {
        if (utenteLoggato == null) {
            System.err.println("Dati utenti mancanti!");
            return;
        }
        try {
            //Salvataggio locale SINGOLO UTENTE
            String pathFile = GestoreDatabase.getPathDatabase("datiUtente.txt");
            File file = new File(pathFile);
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Tipo,NomeUtente,Password\n");
                String riga = String.join(",", utenteLoggato.getTipo(), utenteLoggato.getNomeUtente(), utenteLoggato.getPassword());
                writer.append(riga).append("\n");
            }
            //Invia al Server TUTTI GLI UTENTI
            try (Connection conn = Connessione_SQL.creaConnessione()) {
                String query = "INSERT INTO utenti (nome_utente, password, tipo) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE password = VALUES(password), tipo = VALUES(tipo)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, utenteLoggato.getNomeUtente());
                    stmt.setString(2, utenteLoggato.getPassword());
                    stmt.setString(3, utenteLoggato.getTipo());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Errore nella connessione al database:\n" + e.getMessage());
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Errore nell'esportazione locale:\n" + e.getMessage());
        }
    }

    public static void salvaPrezzi() {
        try {
            String pathFile = GestoreDatabase.getPathDatabase("tariffe.txt");
            File file = new File(pathFile);
            try (FileWriter writer = new FileWriter(file)) {
                StringBuilder sb = new StringBuilder();
                sb.append(Prezzi.costoOrario).append("\n");
                Prezzi.prezziBaseVeicoli.forEach((k, v) -> sb.append("VEICOLO;").append(k).append(";").append(v).append("\n"));
                Prezzi.prezziOpzioni.forEach((k, v) -> sb.append("OPZIONE;").append(k).append(";").append(v).append("\n"));
                Prezzi.prezziGiorni.forEach((k, v) -> sb.append("GIORNO;").append(k).append(";").append(v).append("\n"));
                writer.write(sb.toString());
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Errore nell'esportazione delle tariffe:\n" + e.getMessage());
        }
    }

    public static void caricaPrezzi() {
        try {
            String pathFile = GestoreDatabase.getPathDatabase("tariffe.txt");
            File file = new File(pathFile);
            if (!file.exists()) return;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Prezzi.costoOrario = Double.parseDouble(reader.readLine());
                Prezzi.prezziBaseVeicoli.clear();
                Prezzi.prezziOpzioni.clear();
                Prezzi.prezziGiorni.clear();
                String riga;
                while ((riga = reader.readLine()) != null) {
                    String[] campi = riga.split(";");
                    if (campi.length != 3) continue;
                    String tipo = campi[0];
                    String chiave = campi[1];
                    int valore = Integer.parseInt(campi[2]);
                    switch (tipo) {
                        case "VEICOLO" -> Prezzi.prezziBaseVeicoli.put(chiave, valore);
                        case "OPZIONE" -> Prezzi.prezziOpzioni.put(Integer.parseInt(chiave), valore);
                        case "GIORNO" -> Prezzi.prezziGiorni.put(DayOfWeek.valueOf(chiave), valore);
                    }
                }
            }
        } catch (IOException | URISyntaxException | NumberFormatException e) {
            System.err.println("Errore nel caricamento tariffe:\n" + e.getMessage());
        }
    }

    public static void salvaOrari(Double apertura, Double chiusura, Set<LocalDate> giorniChiusura) {
        try {
            String pathFile = GestoreDatabase.getPathDatabase("orari.txt");
            File file = new File(pathFile);
            try (FileWriter scrivi = new FileWriter(file)) {
                scrivi.write(apertura +"\n");
                scrivi.write(chiusura +"\n");
                if(giorniChiusura!=null && !giorniChiusura.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (LocalDate d : giorniChiusura) {
                        sb.append(d.toString()).append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    scrivi.write(sb.toString());
                } else {
                    scrivi.write("null");
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Errore nell'esportazione degli orari:\n" + e.getMessage());
        }
    }

    public static void caricaOrari() {
        try {
            String pathFile = GestoreDatabase.getPathDatabase("orari.txt");
            File file = new File(pathFile);
            if (!file.exists()) return;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String aperturaString = reader.readLine();
                String chiusuraString = reader.readLine();
                String giorniChiusuraString = reader.readLine();
                if (aperturaString == null || chiusuraString == null) return;
                Double apertura = Double.parseDouble(aperturaString);
                Double chiusura = Double.parseDouble(chiusuraString);
                InterfacciaHelper.setOrariParcheggi(apertura,chiusura);
                if (giorniChiusuraString != null && !giorniChiusuraString.equals("null")) {
                    Set<LocalDate> giorniChiusura = new HashSet<>();
                    String[] parts = giorniChiusuraString.split(",");
                    for (String part : parts) {
                        giorniChiusura.add(LocalDate.parse(part.trim()));
                    }
                    InterfacciaHelper.setGiorniChiusura(giorniChiusura);
                } else {
                    InterfacciaHelper.setGiorniChiusura(null);
                }
            }
        } catch (IOException | URISyntaxException | NumberFormatException e) {
            System.err.println("Errore nel caricamento degli orari:\n" + e.getMessage());
        }
    }
}