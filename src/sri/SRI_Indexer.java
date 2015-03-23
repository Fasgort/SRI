package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
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

        System.out.println("Indexing starts here.");
        System.out.println();

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Lectura de configuración
        ConfigReader configReader = ConfigReader.getInstance(args[0]);
        if (configReader.fail()) {
            return;
        }

        // Desactivando y limpiando serializado
        if ("false".equals(configReader.getSerialize())) {

            // Borramos los diccionarios e índices
            File dirIndex = new File(configReader.getStringDirIndex());
            if (dirIndex.isDirectory()) {
                File[] arrayFiles = dirIndex.listFiles();
                if (arrayFiles.length > 0) {
                    for (File file : arrayFiles) {
                        file.delete();
                    }
                }
                dirIndex.delete();
            }

        }

        int numWords = 0;
        int minNumWords = Integer.MAX_VALUE;
        int maxNumWords = Integer.MIN_VALUE;

        int numWords2 = 0;
        int minNumWords2 = Integer.MAX_VALUE;
        int maxNumWords2 = Integer.MIN_VALUE;

        // DATA STRUCTURES
        DataManager dataManager = DataManager.getInstance();
        Set<String> stopWordSet = new HashSet(1000); // Stop Word Dictionary

        // Lectura de directorio
        File dirHTML = new File(configReader.getStringDirColEn());
        dirHTML.mkdir();

        //Lectura de ficheros
        File[] arrayHTMLFiles = dirHTML.listFiles();

        // Filtrado HTML
        for (File HTMLFile : arrayHTMLFiles) {

            String file = HTMLFile.getName();

            if (!Pattern.matches(".*[.][hH][tT][mM][lL]", file)) {
                continue;
            }

            ArrayList<String> tokenList;
            Integer idFile = dataManager.searchFile(file);
            Checksum checksum;
            boolean modified = false;

            // Generar checksum
            if ("true".equals(configReader.getSerialize())) {
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

            }

            if (modified) {

                String textFiltered = HTMLfilter.filterEN(configReader.getStringDirColEn(), file);
                if (textFiltered == null) {
                    dataManager.ignoreFile(idFile);
                    if ("true".equals(configReader.getDebug())) {
                        System.out.println("File " + file + " was ignored and won't be included in the SE.");
                        System.out.println("Renamed to " + file + ".nocontent");
                    }
                    File ignored = new File(configReader.getStringDirColEn() + file + ".nocontent");
                    HTMLFile.renameTo(ignored);
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

                File dirStem = new File(configReader.getStringDirColEnStem());
                dirStem.mkdir();
                try (FileWriter wr = new FileWriter(configReader.getStringDirColEnStem() + file.replace(".html", ".txt"))) {
                    for (String j : tokenList) {
                        wr.write(j + "\n");
                        Integer idWord = dataManager.searchWord(j);
                        dataManager.addFrequency(idFile, idWord);
                    }
                } catch (Exception e) {
                    System.out.println("Failed saving stemmed file " + file);
                }
                // Fin Módulo Stemmer

            }

        }

        // Generación del índice, con su tabla de pesos normalizada
        dataManager.generateIndex();

        // Serializa y guarda los diccionarios
        if ("true".equals(configReader.getSerialize())) {
            dataManager.saveDictionary();
        }

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        int topFrequentWordsSize = 5;

        if ("false".equals(configReader.getSerialize())) { // Si estamos usando ficheros serializados, no podemos generar estas estadísticas.
            System.out.println("Number of words after filtering: " + numWords);
            System.out.println("Average words after filtering: " + numWords / arrayHTMLFiles.length);
            System.out.println("Min number of words after filtering in documents: " + minNumWords);
            System.out.println("Max Number of words after filtering in documents: " + maxNumWords);
            System.out.println();

            System.out.println("Number of words after cleaning: " + numWords2);
            System.out.println("Average words after cleaning: " + numWords2 / arrayHTMLFiles.length);
            System.out.println("Min number of words after cleaning in documents: " + minNumWords2);
            System.out.println("Max Number of words after cleaning in documents: " + maxNumWords2);
            System.out.println();
        }

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
