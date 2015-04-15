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
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

/**
 *
 * @author Fasgort
 */
public class FileIndexer implements Runnable {

    private String HTMLFileName;
    private Set<String> stopWordSet;

    public FileIndexer(Set<String> stopWord, String filename) {
        HTMLFileName = filename;
        stopWordSet = stopWord;
    }

    @Override
    public void run() {

        ConfigReader configReader = ConfigReader.getInstance();

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
                return;
            }

        }

        if (!filePath.toFile().isFile()) {
            return;
        }

        if (!(HTMLFileName.toLowerCase().endsWith(".html") || HTMLFileName.toLowerCase().endsWith(".htm"))) {
            return;
        }

        DataManager dataManager = DataManager.getInstance();

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

        if (modified || !configReader.getSerialize()) {

            String textFiltered = HTMLFilter.filterEN(filePath);
            if (textFiltered == null) {
                dataManager.ignoreFile(idFile);
                if (configReader.getDebug()) {
                    System.out.println("File " + HTMLFileName + " was ignored and won't be included in the SE.");
                    System.out.println("Renamed to " + HTMLFileName + ".nocontent");
                }
                File HTMLFile = filePath.toFile();
                File ignored = new File(configReader.getStringDirColEn() + HTMLFileName + ".nocontent");
                HTMLFile.renameTo(ignored);
                return;
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
            // Fin Filtrado HTML

            // Módulo Stopper
            tokenList = HTMLFilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());
            if (tokenList == null) {
                return;
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
            // Fin Módulo Stopper

            // Módulo Stemmer
            tokenList = HTMLFilter.stemmer(tokenList);
            if (tokenList == null) {
                return;
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
