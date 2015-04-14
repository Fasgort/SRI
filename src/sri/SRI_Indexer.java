package sri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
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
        String[] arrayHTMLFiles = dirHTML.list();

        // Filtrado HTML
        if (arrayHTMLFiles != null) {
            for (String HTMLFileName : arrayHTMLFiles) {

                Path filePath = FileSystems.getDefault().getPath(configReader.getStringDirColEn(), HTMLFileName);

                {
                    int tries = 0;
                    boolean skipped = false;

                    while (Files.isSymbolicLink(filePath)) {

                        try {
                            filePath = filePath.resolveSibling(Files.readSymbolicLink(filePath));
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                        Path pathName = filePath.getFileName();
                        if (pathName == null) {
                            skipped = true;
                            break;
                        }
                        HTMLFileName = pathName.toString();
                        tries++;

                        if (tries >= 10) {
                            System.out.println("Is this a soft link loop? You smartass. I'm skipping it.");
                            skipped = true;
                            break;
                        }
                    }

                    if (skipped) {
                        continue;
                    }

                }

                if (!filePath.toFile().isFile()) {
                    continue;
                }

                if (!(HTMLFileName.toLowerCase().endsWith(".html") || HTMLFileName.toLowerCase().endsWith(".htm"))) {
                    continue;
                }

                ArrayList<String> tokenList;
                Integer idFile = dataManager.searchFile(HTMLFileName);
                Checksum checksum;
                boolean modified = false;

                // Generar checksum
                if (configReader.getSerialize()) {
                    try (InputStream fis = new FileInputStream(filePath.toString())) {
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
                            if (configReader.getDebug()) {
                                System.out.println("File " + HTMLFileName + " was modified.");
                            }
                            dataManager.updateChecksumFile(idFile, checksum.getValue());
                        }

                    } catch (IOException ex) {
                        System.err.println(ex);
                    }

                }

                if (modified) {

                    String textFiltered = HTMLFilter.filterEN(filePath);
                    if (textFiltered == null) {
                        dataManager.ignoreFile(idFile);
                        if (configReader.getDebug()) {
                            System.out.println("File " + HTMLFileName + " was ignored and won't be included in the SE.");
                            //System.out.println("Renamed to " + HTMLFileName + ".nocontent");
                        }
                        //File HTMLFile = filePath.toFile();
                        //File ignored = new File(configReader.getStringDirColEn() + HTMLFileName + ".nocontent");
                        //HTMLFile.renameTo(ignored);
                        continue;
                    }

                    tokenList = HTMLFilter.normalize(textFiltered);

                    File dirNorm = new File(configReader.getStringDirColEnN());
                    dirNorm.mkdir();
                    try (FileWriter wr = new FileWriter(configReader.getStringDirColEnN() + HTMLFileName.replace(".html", ".txt"))) {
                        for (String j : tokenList) {
                            wr.write(j + "\n");
                        }
                    } catch (IOException ex) {
                        System.err.println(ex);
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
                    tokenList = HTMLFilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());
                    if (tokenList == null) {
                        continue;
                    }

                    File dirStop = new File(configReader.getStringDirColEnStop());
                    dirStop.mkdir();
                    try (FileWriter wr = new FileWriter(configReader.getStringDirColEnStop() + HTMLFileName.replace(".html", ".txt"))) {
                        for (String j : tokenList) {
                            wr.write(j + "\n");
                        }
                    } catch (IOException ex) {
                        System.err.println(ex);
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
                    tokenList = HTMLFilter.stemmer(tokenList);
                    if (tokenList == null) {
                        continue;
                    }

                    File dirStem = new File(configReader.getStringDirColEnStem());
                    dirStem.mkdir();
                    try (FileWriter wr = new FileWriter(configReader.getStringDirColEnStem() + HTMLFileName.replace(".html", ".txt"))) {
                        for (String j : tokenList) {
                            wr.write(j + "\n");
                            Integer idWord = dataManager.searchWord(j);
                            dataManager.addFrequency(idFile, idWord);
                        }
                    } catch (IOException ex) {
                        System.err.println(ex);
                    }
                    // Fin Módulo Stemmer

                }

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

        if (!configReader.getSerialize()) { // Si estamos usando serializado, no podemos generar estas estadísticas.
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
