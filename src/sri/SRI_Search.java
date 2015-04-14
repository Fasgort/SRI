package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Fasgort
 */
public class SRI_Search {

    /**
     * @param args the command line arguments
     */
    public static void run(String[] args) {

        System.out.println("Searching starts here.");
        System.out.println();

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Lectura de configuración
        ConfigReader configReader = ConfigReader.getInstance(args[0]);
        if (configReader.fail()) {
            return;
        }

        // DATA STRUCTURES
        DataManager dataManager = DataManager.getInstance();
        Set<String> stopWordSet = new HashSet(1000); // Stop Word Dictionary

        // Lectura del fichero de consultas
        ArrayList<String> tokenList;
        String searchString;

        // Función localizada
        {
            File searchInput = new File(configReader.getStringSearchFile());
            StringBuilder build = new StringBuilder();

            // Extracción de los términos de búsqueda
            try (FileReader fr = new FileReader(searchInput);
                    BufferedReader br = new BufferedReader(fr);) {
                String line;
                while ((line = br.readLine()) != null) {
                    build.append(line);
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }

            searchString = build.toString();

        }

        System.out.println("Search input was: \"" + searchString + "\"");
        System.out.println();

        // Módulo Normalize
        tokenList = HTMLFilter.normalize(searchString);

        // Módulo Stopper
        tokenList = HTMLFilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());

        // Módulo Stemmer
        tokenList = HTMLFilter.stemmer(tokenList);

        // Búsqueda
        dataManager.searchResults(tokenList);

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        System.out.println("Searching was completed in " + (end - start) + " milliseconds.");
        System.out.println("Searching ends here.");
        System.out.println();
        System.out.println();

    }

}
