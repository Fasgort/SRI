package sri;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Fasgort
 */
public class SRI_Indexer {

    /**
     * @param args the command line arguments
     */
    public static void run(String[] args) {

        System.out.println("Indexing starts here.");
        System.out.println();

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Lectura de configuración
        ConfigReader configReader = ConfigReader.getInstance(args[0]);
        if (configReader.fail(args[0])) {
            return;
        }

        // Desactivando y limpiando serializado
        if (!configReader.getSerialize()) {

            // Borramos los diccionarios e índices
            File dirIndex = new File(configReader.getStringDirIndex());
            if (dirIndex.isDirectory()) {
                File[] arrayFiles = dirIndex.listFiles();
                if (arrayFiles != null) {
                    if (arrayFiles.length > 0) {
                        for (File file : arrayFiles) {
                            file.delete();
                        }
                    }
                }
                dirIndex.delete();
            }

        }

        // DATA STRUCTURES
        DataManager dataManager = DataManager.getInstance();
        Set<String> stopWordSet = new HashSet(1000); // Stop Word Dictionary

        // Lectura de directorio
        File dirHTML = new File(configReader.getStringDirColEn());
        dirHTML.mkdir();

        //Lectura de ficheros
        String[] arrayHTMLFiles = dirHTML.list();

        // Filtrado HTML
        if (arrayHTMLFiles != null) {

            // Procesamiento multithread
            ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (String HTMLFileName : arrayHTMLFiles) {
                Runnable documentThread = new FileIndexer(stopWordSet, HTMLFileName);
                exec.execute(documentThread);
            }

            try {
                exec.shutdown();
                exec.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                System.err.println(ex);
            }

        }

        // Generación del índice, con su tabla de pesos normalizada
        dataManager.generateIndex();

        // Serializa y guarda los diccionarios
        if (configReader.getSerialize()) {
            dataManager.saveDictionary();
        }

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        int topFrequentWordsSize = 5;

        System.out.println("Number of registered words: " + dataManager.wordQuantity());
        System.out.println("Number of processed documents: " + dataManager.fileQuantity());
        dataManager.topFrequentWords(topFrequentWordsSize);
        System.out.println();
        // Fin Estadísticas

        System.out.println("Indexing was completed in " + (end - start) + " milliseconds.");
        System.out.println("Indexing ends here.");
        System.out.println();
        System.out.println();

    }

}
