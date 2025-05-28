package listati;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

// Un record funziona come una classe, senza set, ma solo con get e metodo costruttore
public record Posto(Utente utente, String nome, String cognome, String targa, String tipo, double costo,
                    LocalDateTime dataArrivo, LocalDateTime dataPartenza,
                    int x, int y) {

    public static Posto fromResultSet(ResultSet rs) throws SQLException {
        String nomeUtente = rs.getString("nome_utente");
        Utente u = new Utente("Utente", nomeUtente, "");

        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String targa = rs.getString("targa");
        String tipo = rs.getString("tipo");
        double costo = rs.getDouble("costo");
        LocalDateTime arrivo = LocalDateTime.of(
                rs.getDate("data_arrivo").toLocalDate(),
                rs.getTime("ora_arrivo").toLocalTime()
        );
        LocalDateTime partenza = LocalDateTime.of(
                rs.getDate("data_partenza").toLocalDate(),
                rs.getTime("ora_partenza").toLocalTime()
        );
        int x = rs.getInt("xPosto");
        int y = rs.getInt("yPosto");
        return new Posto(u, nome, cognome, targa, tipo, costo, arrivo, partenza, x, y);
    }
}
