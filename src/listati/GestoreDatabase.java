package listati;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;

public class GestoreDatabase {
    private static final String CARTELLA_ESTERNA = System.getProperty("user.home") + File.separator + "ParcheggioAPP" + File.separator + "database";

    public static String getPathDatabase(String nomeFile) throws IOException, URISyntaxException {
        if (isJar()) {
            creaCartellaEsternaSeNonEsiste();
            copiaFileSeNonEsiste(nomeFile);
            return CARTELLA_ESTERNA + File.separator + nomeFile;
        } else {
            // SVILUPPO
            return "src/risorse/database/" + nomeFile;
        }
    }

    public static String getPathConfig(String nomeFile) throws IOException {
        if (isJar()) {
            creaCartellaEsternaSeNonEsiste();
            copiaFileSeNonEsiste(nomeFile);
            return CARTELLA_ESTERNA + File.separator + nomeFile;
        } else {
            // SVILUPPO
            return "src/" + nomeFile;
        }
    }

    private static boolean isJar() {
        URL url = GestoreDatabase.class.getResource("GestoreDatabase.class");
        return url != null && url.toString().startsWith("jar:");
    }

    private static void creaCartellaEsternaSeNonEsiste() throws IOException {
        Path path = Paths.get(CARTELLA_ESTERNA);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private static void copiaFileSeNonEsiste(String nomeFile) throws IOException {
        Path fileEsterno = Paths.get(CARTELLA_ESTERNA, nomeFile);
        if (!Files.exists(fileEsterno)) {
            // Copia il file risorsa dal jar a cartella esterna
            try (InputStream is = GestoreDatabase.class.getResourceAsStream("/database/" + nomeFile)) {
                if (is == null) {
                    throw new FileNotFoundException("Risorsa interna non trovata: " + nomeFile);
                }
                Files.copy(is, fileEsterno);
            }
        }
    }
}

