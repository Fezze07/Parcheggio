package listati;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestioneUtenti {
    private static Connection conn;

    public static void inizializzaConnessione() {
        try {
            conn = Connessione_SQL.creaConnessione();
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database esterno");
        }
    }

    public void aggiungiUtente(Utente utente) {
        if (utente == null) {
            System.err.println("Utente nullo, non posso aggiungerlo");
            return;
        }
        SalvaCarica.esportaUtenti(utente);
    }

    public static void cancellaUtente(Utente utente) {
        String query = "DELETE FROM utenti WHERE nome_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            System.out.println("Query: " + query);
            System.out.println("Parametro: " + utente.getNomeUtente());
            stmt.setString(1, utente.getNomeUtente());
            int righe = stmt.executeUpdate();
            if (righe == 0) {
                InterfacciaHelper.mostraErrore("Utente non trovato");
            } else {
                InterfacciaHelper.mostraConferma("Utente eliminato con successo!");
            }
        } catch (SQLException e) {
            System.err.println("Errore cancellazione utente: " + e.getMessage());
        }
    }

    public Utente ritornaUtente(String nome) {
        String query = "SELECT nome_utente, password, tipo FROM utenti WHERE nome_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nomeUtente = rs.getString("nome_utente");
                    String password = rs.getString("password");
                    String tipo = rs.getString("tipo");
                    return new Utente(tipo, nomeUtente, password);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel ritorno utente: " + e.getMessage());
        }
        return null;
    }


    public boolean verificaCredenziali(String nomeUtente, String password) {
        Utente u = ritornaUtente(nomeUtente);
        if (u == null) {
            InterfacciaHelper.mostraErrore("Utente non trovato!");
            return false;
        }
        if (u.getPassword().equals(password)) return true;
        InterfacciaHelper.mostraErrore("Password non corretta");
        return false;
    }

    public boolean verificaDisponibilitaNome(String nome) {
        String query = "SELECT 1 FROM utenti WHERE nome_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Errore verifica disponibilità nome: " + e.getMessage());
            return false;
        }
    }

    public static void cambiaPassword(Utente utente, String nuovaPassword) {
        String query = "UPDATE utenti SET password = ? WHERE nome_utente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nuovaPassword);
            stmt.setString(2, utente.getNomeUtente());
            int righe = stmt.executeUpdate();
            if (righe > 0) utente.setPassword(nuovaPassword);
            else System.err.println("Utente non trovato per cambio password");
        } catch (SQLException e) {
            System.err.println("Errore cambio password: " + e.getMessage());
        }
    }

    public static void cancellaUtenteAdmin(Utente utente) {
        FunzioniAdmin.cancellaUtente(utente, conn);
    }
}