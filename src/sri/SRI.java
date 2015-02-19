package sri;

import java.io.*;
import java.util.*;

/**
 *
 * @author Fasgort
 */
public class SRI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int numWords = 0;
        int minNumWords = Integer.MAX_VALUE;
        int maxNumWords = Integer.MIN_VALUE;

        int numWords2 = 0;
        int minNumWords2 = Integer.MAX_VALUE;
        int maxNumWords2 = Integer.MIN_VALUE;

        Set<String> stopWordSet = new HashSet(800);
        Map<String, FrequentWord> cleanedWords = new HashMap(25000);
        Map<String, FrequentWord> stemmedWords = new HashMap(20000);

        String debug;
        String dirResources;
        String stopWordFilename;
        String stringDirColEn;
        String stringDirColEnN;
        String stringDirColEnStop;
        String stringDirColEnStem;

        // Lectura de configuración
        File confData = new File("./conf.data");

        try (FileReader fr = new FileReader(confData);
                BufferedReader br = new BufferedReader(fr);) {

            debug = br.readLine();
            dirResources = br.readLine();
            stopWordFilename = br.readLine();
            stringDirColEn = br.readLine();
            stringDirColEnN = br.readLine();
            stringDirColEnStop = br.readLine();
            stringDirColEnStem = br.readLine();

            br.close();

        } catch (Exception e) {
            System.out.println("Config file couldn't load. It must be included with the executable.");
            return;
        }
        // Fin Lectura de configuración

        File dirHTML = new File(stringDirColEn);

        File[] arrayHTMLfile = dirHTML.listFiles();

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Filtrado HTML
        for (File arrayHTMLfile1 : arrayHTMLfile) {

            ArrayList<String> tokenList;

            String file = arrayHTMLfile1.getName();
            String textFiltered = HTMLfilter.filterEN(stringDirColEn, file);
            if (textFiltered == null) {
                if (debug.contentEquals("enabled")) {
                    System.out.println("File " + file + " was ignored and won't be included in the SE.");
                }
                continue;
            }

            tokenList = HTMLfilter.normalize(textFiltered);

            File dirNorm = new File(stringDirColEnN);
            dirNorm.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnN + file.replace(".html", ".txt"))) {
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
            tokenList = HTMLfilter.stopper(tokenList, stopWordSet, dirResources, stopWordFilename);
            if (tokenList == null) {
                continue;
            }

            File dirStop = new File(stringDirColEnStop);
            dirStop.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnStop + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                    FrequentWord fw = cleanedWords.get(j);
                    if (fw == null) {
                        cleanedWords.put(j, new FrequentWord(j));
                    } else {
                        fw.addCount();
                    }
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

            File dirStem = new File(stringDirColEnStem);
            dirStem.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnStem + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                    FrequentWord fw = stemmedWords.get(j);
                    if (fw == null) {
                        stemmedWords.put(j, new FrequentWord(j));
                    } else {
                        fw.addCount();
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed saving stemmed file " + file);
            }
            // Fin Módulo Stemmer

        }

        // Generación de listas de palabras frecuentes
        PriorityQueue<FrequentWord> frequentCleanedWords = new PriorityQueue(cleanedWords.values());
        PriorityQueue<FrequentWord> frequentStemmedWords = new PriorityQueue(stemmedWords.values());

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        FrequentWord fw;

        System.out.println(
                "Operation was completed in " + (end - start) + " milliseconds.");
        System.out.println();

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
                "Number of unique words after cleaning: " + cleanedWords.size());
        System.out.println(
                "Average words after cleaning: " + numWords2 / arrayHTMLfile.length);
        System.out.println(
                "Min number of words after cleaning in documents: " + minNumWords2);
        System.out.println(
                "Max Number of words after cleaning in documents: " + maxNumWords2);
        System.out.println(
                "Top 5 frequent words after cleaning: ");
        fw = frequentCleanedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentCleanedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentCleanedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentCleanedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentCleanedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        System.out.println();

        System.out.println(
                "Number of unique words after stemming: " + stemmedWords.size());
        System.out.println(
                "Average unique words after stemming: " + stemmedWords.size() / arrayHTMLfile.length);
        System.out.println(
                "Top 5 frequent words after stemming: ");
        fw = frequentStemmedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentStemmedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentStemmedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentStemmedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        fw = frequentStemmedWords.poll();
        System.out.println("   " + fw.getWord() + " with " + fw.getCount() + " apparitions in documents.");
        System.out.println();
        // Fin Estadísticas

    }

}
