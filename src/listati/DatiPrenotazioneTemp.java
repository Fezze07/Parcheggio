package listati;

import java.time.LocalDate;
import java.util.List;

public class DatiPrenotazioneTemp {
    public String nome, cognome, targa, tipo;
    public LocalDate data;
    public double orarioArrivo, orarioPartenza;
    public int XPosto, YPosto;
    public boolean usaOpzioni;
    public List<String> opzioniSelezionate;

    public DatiPrenotazioneTemp(String nome, String cognome, String targa, String tipo, LocalDate data,
                                double orarioArrivo, double orarioPartenza,
                                int XPosto, int YPosto,
                                boolean usaOpzioni, List<String> opzioniSelezionate) {
        this.nome = nome;
        this.cognome = cognome;
        this.targa = targa; this.tipo = tipo;
        this.data = data;
        this.orarioArrivo = orarioArrivo; this.orarioPartenza = orarioPartenza;
        this.XPosto = XPosto; this.YPosto = YPosto;
        this.usaOpzioni = usaOpzioni;
        this.opzioniSelezionate = opzioniSelezionate;
    }
}


