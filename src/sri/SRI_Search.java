package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        if (configReader.fail(args[0])) {
            return;
        }

        // DATA STRUCTURES
        Set<String> stopWordSet = new HashSet(1000); // Stop Word Dictionary

        // Lectura del fichero de consultas
        ArrayList<String> searchList = new ArrayList();

        // Función localizada
        {
            File searchInput = new File(configReader.getStringSearchFile());

            // Extracción de los términos de búsqueda
            try (FileReader fr = new FileReader(searchInput);
                    BufferedReader br = new BufferedReader(fr);) {
                String line;
                while ((line = br.readLine()) != null) {
                    searchList.add(line);
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }

        // Procesamiento multithread
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        searchList.stream().map((searchString) -> new SearchThread(stopWordSet, searchString)).forEach((searchThread) -> {
            exec.execute(searchThread);
        });

        try {
            exec.shutdown();
            exec.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        System.out.println("Searching was completed in " + (end - start) + " milliseconds.");
        System.out.println("Searching ends here.");
        System.out.println();
        System.out.println();

    }

}
