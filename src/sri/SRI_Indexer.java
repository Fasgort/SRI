package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 *
 * @author Fasgort
 */
public class SRI_Indexer {

    /**
     * @param args the command line arguments
     */
    public static void run(String[] args) {

        // Lectura de configuración
        ConfigReader configReader = ConfigReader.getInstance(args[0]);
        if (configReader.fail()) {
            return;
        }

        int numWords = 0;
        int minNumWords = Integer.MAX_VALUE;
        int maxNumWords = Integer.MIN_VALUE;

        int numWords2 = 0;
        int minNumWords2 = Integer.MAX_VALUE;
        int maxNumWords2 = Integer.MIN_VALUE;

        // DATA STRUCTURES
        DataManager dataManager = DataManager.getInstance();
        Set<String> stopWordSet = new HashSet(800); // Stop Word Dictionary

        // Lectura de directorio
        File dirHTML = new File(configReader.getStringDirColEn());
        dirHTML.mkdir();

        //Lectura de ficheros
        File[] arrayHTMLfile = dirHTML.listFiles();

        // No existen ficheros en el directorio
        if (arrayHTMLfile.length == 0) {
            System.out.println("No files to filter. Exiting program.");
            return;
        }

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Filtrado HTML
        for (File arrayHTMLfile1 : arrayHTMLfile) {

            ArrayList<String> tokenList = null;
            String file = arrayHTMLfile1.getName();
            Integer idFile = dataManager.searchFile(file);
            boolean skip = false;
            Checksum checksum;
            boolean modified = false;

            // Generar checksum
            try (InputStream fis = new FileInputStream(configReader.getStringDirColEn() + file)) {
                byte[] buffer = new byte[1024];
                checksum = new Adler32();
                int numRead;
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        checksum.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);

                // Comprobar diferencias en el archivo
                if (!dataManager.checksumFile(idFile, checksum.getValue())) {
                    modified = true;
                    if ("true".equals(configReader.getDebug())) {
                        System.out.println("File " + file + " was modified.");
                    }
                    dataManager.updateChecksumFile(idFile, checksum.getValue());
                }

            } catch (Exception e) {
                System.out.println("Checksum failed.");
            }

            File serializedFile = new File(configReader.getStringDirColEnSer() + file.replace(".html", ".ser"));
            if (serializedFile.canRead() && "true".equals(configReader.getSerialize()) && !modified) {
                try {
                    FileInputStream fis = new FileInputStream(serializedFile);
                    try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                        tokenList = (ArrayList<String>) ois.readObject();
                        skip = true;
                    }
                } catch (Exception e) {
                    System.out.println("Failed loading serialized stemmed file " + file);
                }
            }

            if (!skip) {

                String textFiltered = HTMLfilter.filterEN(configReader.getStringDirColEn(), file);
                if (textFiltered == null) {
                    if ("true".equals(configReader.getDebug())) {
                        System.out.println("File " + file + " was ignored and won't be included in the SE.");
                    }
                    continue;
                }

                tokenList = HTMLfilter.normalize(textFiltered);

                File dirNorm = new File(configReader.getStringDirColEnN());
                dirNorm.mkdir();
                try (FileWriter wr = new FileWriter(configReader.getStringDirColEnN() + file.replace(".html", ".txt"))) {
                    for (String j : tokenList) {
                        wr.write(j + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("Failed saving normalised file " + file);
                }

                numWords += tokenList.size();
                if (tokenList.size() > maxNumWords) {
                    maxNumWords = tokenList.size();
                }
                if (tokenList.size() < minNumWords) {
                    minNumWords = tokenList.size();
                }
                // Fin Filtrado HTML

                // Módulo Stopper
                tokenList = HTMLfilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());
                if (tokenList == null) {
                    continue;
                }

                File dirStop = new File(configReader.getStringDirColEnStop());
                dirStop.mkdir();
                try (FileWriter wr = new FileWriter(configReader.getStringDirColEnStop() + file.replace(".html", ".txt"))) {
                    for (String j : tokenList) {
                        wr.write(j + "\n");
                    }
                } catch (Exception e) {
                    System.out.println("Failed saving cleaned file " + file);
                }

                numWords2 += tokenList.size();
                if (tokenList.size() > maxNumWords2) {
                    maxNumWords2 = tokenList.size();
                }
                if (tokenList.size() < minNumWords2) {
                    minNumWords2 = tokenList.size();
                }
                // Fin Módulo Stopper

                // Módulo Stemmer
                tokenList = HTMLfilter.stemmer(tokenList);
                if (tokenList == null) {
                    continue;
                }

                if ("true".equals(configReader.getSerialize())) {
                    File dirSer = new File(configReader.getStringDirColEnSer());
                    dirSer.mkdir();
                    try {
                        FileOutputStream fos = new FileOutputStream(configReader.getStringDirColEnSer() + file.replace(".html", ".ser"));
                        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                            oos.writeObject(tokenList);
                        }
                    } catch (Exception e) {
                        System.out.println("Failed serializing stemmed file " + file);
                    }
                }

                File dirStem = new File(configReader.getStringDirColEnStem());
                dirStem.mkdir();
                try (FileWriter wr = new FileWriter(configReader.getStringDirColEnStem() + file.replace(".html", ".txt"))) {
                    for (String j : tokenList) {
                        wr.write(j + "\n");
                        Integer idWord = dataManager.searchWord(j);
                        dataManager.addFrequency(idWord, idFile);
                    }
                } catch (Exception e) {
                    System.out.println("Failed saving stemmed file " + file);
                }
                // Fin Módulo Stemmer

            } else {
                for (String j : tokenList) {
                    Integer idWord = dataManager.searchWord(j);
                    dataManager.addFrequency(idWord, idFile);
                }
            }

        }

        // Generación del índice, con su tabla de pesos normalizada
        dataManager.generateIndex();

        // Serializa y guarda los diccionarios
        if ("true".equals(configReader.getSerialize())) {
            dataManager.saveDictionary(configReader.getStringDirDictionary());
        }

        // Generación de listas de palabras frecuentes
        LinkedList<WordData> topFrequentWords = dataManager.topFrequentWords(5);
        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        WordData wd;

        System.out.println(
                "Operation was completed in " + (end - start) + " milliseconds.");
        System.out.println();

        if ("false".equals(configReader.getSerialize())) { // Si estamos usando ficheros serializados, no podemos generar estas estadísticas.
            System.out.println(
                    "Number of words after filtering: " + numWords);
            System.out.println(
                    "Average words after filtering: " + numWords / arrayHTMLfile.length);
            System.out.println(
                    "Min number of words after filtering in documents: " + minNumWords);
            System.out.println(
                    "Max Number of words after filtering in documents: " + maxNumWords);
            System.out.println();

            System.out.println(
                    "Number of words after cleaning: " + numWords2);
            System.out.println(
                    "Average words after cleaning: " + numWords2 / arrayHTMLfile.length);
            System.out.println(
                    "Min number of words after cleaning in documents: " + minNumWords2);
            System.out.println(
                    "Max Number of words after cleaning in documents: " + maxNumWords2);
            System.out.println();
        }

        System.out.println(
                "Number of unique words after stemming: " + dataManager.wordQuantity());
        System.out.println(
                "Average unique words after stemming: " + dataManager.wordQuantity() / arrayHTMLfile.length);
        System.out.println(
                "Top 5 frequent words after stemming: ");

        int topSize = topFrequentWords.size();
        for (int i = 0; i < topSize; i++) {
            wd = topFrequentWords.removeFirst();
            System.out.println("   " + wd.getWord() + " with " + wd.getWordCount() + " apparitions in documents.");
        }
        System.out.println();
        // Fin Estadísticas

    }

}
