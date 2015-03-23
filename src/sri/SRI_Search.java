package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

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
        File searchInput = new File(configReader.getStringSearchFile());
        String searchString = "";

        // Extracción de los términos de búsqueda
        ArrayList<String> tokenList = new ArrayList();
        try (FileReader fr = new FileReader(searchInput);
                BufferedReader br = new BufferedReader(fr);) {
            String line;
            while ((line = br.readLine()) != null) {
                searchString = searchString.concat(line);
            }
        } catch (Exception e) {
            System.out.println("Failed loading " + searchInput.getName());
        }
        
        System.out.println("Search input was: " + searchString);
        System.out.println();

        StringTokenizer st = new StringTokenizer(searchString);

        while (st.hasMoreTokens()) {
            String word = st.nextToken();

            if (!word.isEmpty()) {
                tokenList.add(word.toLowerCase());
            }
        }
        // Fin de la extracción

        // Módulo Stopper
        tokenList = HTMLfilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());

        // Módulo Stemmer
        tokenList = HTMLfilter.stemmer(tokenList);

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
