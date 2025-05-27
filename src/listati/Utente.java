package listati;

public class Utente {
    public String nomeUtente;
    public String password;
    public final String tipo;
    public Utente(String tipo, String nomeUtente, String password) {
        this.tipo = tipo;
        this.nomeUtente=nomeUtente;
        this.password=password;
    }

    public void setNomeUtente(String nomeUtente) {
        this.nomeUtente = nomeUtente;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public String getPassword() {
        return password;
    }

    public String getTipo() {
        return tipo;
    }

    public String toString() {
        return tipo + "=  " +nomeUtente+ " Password = " +password;
    }
}
