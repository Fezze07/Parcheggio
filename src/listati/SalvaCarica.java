package listati;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
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

    public static void esportaPrezzi() {
        try (Connection conn = Connessione_SQL.creaConnessione()) {
            conn.setAutoCommit(false);
            //Costo base
            String updateBase = "UPDATE tariffe_base SET costo_quindici_minuti = ? WHERE id = 1";
            try (PreparedStatement stmt = conn.prepareStatement(updateBase)) {
                stmt.setDouble(1, Prezzi.costoOrario);
                stmt.executeUpdate();
            }
            //Tariffe veicoli
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tariffe_veicoli");
            }
            String insertVeicolo = "INSERT INTO tariffe_veicoli (tipo_veicolo, costo) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertVeicolo)) {
                for (var entry : Prezzi.prezziBaseVeicoli.entrySet()) {
                    stmt.setString(1, entry.getKey());
                    stmt.setInt(2, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            //Tariffe opzioni
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tariffe_opzioni");
            }
            String insertOpzione = "INSERT INTO tariffe_opzioni (id, costo) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertOpzione)) {
                for (var entry : Prezzi.prezziOpzioni.entrySet()) {
                    stmt.setInt(1, entry.getKey());
                    stmt.setInt(2, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            //Tariffe giornaliere
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM tariffe_giornaliere");
            }
            String insertGiorno = "INSERT INTO tariffe_giornaliere (giorno_settimana, incremento) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertGiorno)) {
                for (var entry : Prezzi.prezziGiorni.entrySet()) {
                    stmt.setString(1, entry.getKey().name());
                    stmt.setInt(2, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Errore nell'esportazione delle tariffe al database:\n" + e.getMessage());
        }
    }

    public static void caricaPrezzi() {
        try (Connection conn = Connessione_SQL.creaConnessione()) {
            //Tariffa base
            String queryBase = "SELECT costo_quindici_minuti FROM tariffe_base WHERE id = 1";
            try (PreparedStatement stmt = conn.prepareStatement(queryBase);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prezzi.costoOrario = rs.getDouble("costo_quindici_minuti");
                }
            }
            //Tariffe veicoli
            Prezzi.prezziBaseVeicoli.clear();
            String queryVeicoli = "SELECT tipo_veicolo, costo FROM tariffe_veicoli";
            try (PreparedStatement stmt = conn.prepareStatement(queryVeicoli);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo_veicolo");
                    int costo = rs.getInt("costo");
                    Prezzi.prezziBaseVeicoli.put(tipo, costo);
                }
            }
            //Tariffe opzioni
            Prezzi.prezziOpzioni.clear();
            String queryOpzioni = "SELECT id, costo FROM tariffe_opzioni";
            try (PreparedStatement stmt = conn.prepareStatement(queryOpzioni);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int costo = rs.getInt("costo");
                    Prezzi.prezziOpzioni.put(id, costo);
                }
            }
            //Tariffe giornaliere
            Prezzi.prezziGiorni.clear();
            String queryGiorni = "SELECT giorno_settimana, incremento FROM tariffe_giornaliere";
            try (PreparedStatement stmt = conn.prepareStatement(queryGiorni);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DayOfWeek giorno = DayOfWeek.valueOf(rs.getString("giorno_settimana").toUpperCase());
                    int costo = rs.getInt("incremento");
                    Prezzi.prezziGiorni.put(giorno, costo);
                }
            }

        } catch (SQLException e) {
            System.err.println("Errore nel caricamento tariffe dal database:\n" + e.getMessage());
        }
    }

    public static void esportaOrari(Double apertura, Double chiusura, Set<LocalDate> giorniChiusura) {
        //Invia al Server APERTURA/CHIUSURA
        try (Connection conn = Connessione_SQL.creaConnessione()) {
            String query = "INSERT INTO orari (apertura, chiusura) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE apertura = VALUES(apertura), chiusura = VALUES(chiusura)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, apertura.toString());
                stmt.setString(2, chiusura.toString());
                stmt.executeUpdate();
            }
            //Invia al Server GIORNI-CHIUSURA
            String query2 = "INSERT INTO giorni_festivi (data) VALUES (?) " +
                    "ON DUPLICATE KEY UPDATE data = VALUES(data)";
            try (PreparedStatement stmt = conn.prepareStatement(query2)) {
                for (LocalDate giorno : giorniChiusura) {
                    stmt.setDate(1, Date.valueOf(giorno));
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database:\n" + e.getMessage());
        }
    }

    public static void caricaOrariDaDatabase() {
        try (Connection conn = Connessione_SQL.creaConnessione()) {
            String query = "SELECT apertura, chiusura FROM orari WHERE id = 1";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double apertura = rs.getDouble("apertura");
                    double chiusura = rs.getDouble("chiusura");
                    InterfacciaHelper.setOrariParcheggi(apertura, chiusura);
                }
            }
            String query2 = "SELECT data FROM giorni_festivi";
            Set<LocalDate> giorniChiusura = new HashSet<>();
            try (PreparedStatement stmt = conn.prepareStatement(query2);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate data = rs.getDate("data").toLocalDate();
                    giorniChiusura.add(data);
                }
            }
            if (giorniChiusura.isEmpty()) giorniChiusura = null;
            InterfacciaHelper.setGiorniChiusura(giorniChiusura);
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento degli orari dal database:\n" + e.getMessage());
        }
    }
}