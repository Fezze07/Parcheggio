package listati;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class GestioneVisualizzazioneParcheggio {
    private static final int GRANDEZZA_Y = 5;

    public static Set<String> getPostiOccupati(LocalDateTime arrivo, LocalDateTime partenza) {
        Set<String> occupati = new HashSet<>();
        String query = "SELECT xPosto, yPosto, data_arrivo, ora_arrivo, data_partenza, ora_partenza FROM prenotazioni WHERE NOT (data_partenza < ? OR data_arrivo > ?)";
        try (Connection conn = Connessione_SQL.creaConnessione();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(arrivo.toLocalDate()));
            stmt.setDate(2, java.sql.Date.valueOf(partenza.toLocalDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime dbArrivo = LocalDateTime.of(rs.getDate("data_arrivo").toLocalDate(), rs.getTime("ora_arrivo").toLocalTime());
                    LocalDateTime dbPartenza = LocalDateTime.of(rs.getDate("data_partenza").toLocalDate(), rs.getTime("ora_partenza").toLocalTime());
                    if (!partenza.isBefore(dbArrivo) && !arrivo.isAfter(dbPartenza)) {
                        int x = rs.getInt("xPosto");
                        int y = rs.getInt("yPosto");
                        occupati.add(x + "," + y);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database " + e.getMessage());
        }
        return occupati;
    }

    public static int calcolaNumeroPosto(int x, int y) {
        return x * GRANDEZZA_Y + y + 1;
    }

    public static boolean isOccupato(int x, int y, Set<String> occupati) {
        return occupati.contains(x + "," + y);
    }
}