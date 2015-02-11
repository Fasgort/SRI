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
        int numWords2 = 0;
        int minNumWords = Integer.MAX_VALUE;
        int maxNumWords = Integer.MIN_VALUE;
        int minNumWords2 = Integer.MAX_VALUE;
        int maxNumWords2 = Integer.MIN_VALUE;

        Set<String> stopWordSet = new HashSet(800);

        String debug;
        String dirResources;
        String stopWordFilename;
        String stringDirColEn;
        String stringDirColEnN;
        String stringDirColEnS;

        File confData = new File("./conf.data");

        try (FileReader fr = new FileReader(confData);
                BufferedReader br = new BufferedReader(fr);) {

            debug = br.readLine();
            dirResources = br.readLine();
            stopWordFilename = br.readLine();
            stringDirColEn = br.readLine();
            stringDirColEnN = br.readLine();
            stringDirColEnS = br.readLine();

            br.close();

        } catch (Exception e) {
            System.out.println("Config file couldn't load. It must be included with the executable.");
            return;
        }

        File dirHTML = new File(stringDirColEn);

        File[] arrayHTMLfile = dirHTML.listFiles();

        long start = System.currentTimeMillis();

        for (File arrayHTMLfile1 : arrayHTMLfile) {

            ArrayList<String> tokenList;

            String file = arrayHTMLfile1.getName();
            String textFiltered = HTMLfilter.filterEN(stringDirColEn, file);
            if (textFiltered == null) {
                if(debug.contentEquals("enabled")) System.out.println("File " + file + " was ignored and won't be included in the SE.");
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

            tokenList = HTMLfilter.stopper(tokenList, stopWordSet, dirResources, stopWordFilename);
            if (tokenList == null) {
                continue;
            }

            File dirStop = new File(stringDirColEnS);
            dirStop.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnS + file.replace(".html", ".txt"))) {
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
        }

        long end = System.currentTimeMillis();

        System.out.println(
                "Operation was completed in " + (end - start) + " milliseconds.");

        System.out.println(
                "Number of words before cleaning: " + numWords);
        System.out.println(
                "Average words before cleaning: " + numWords / 200);
        System.out.println(
                "Min number of words before cleaning in documents: " + minNumWords);
        System.out.println(
                "Max Number of words before cleaning in documents: " + maxNumWords);

        System.out.println(
                "Number of words after cleaning: " + numWords2);
        System.out.println(
                "Average words after cleaning: " + numWords2 / 200);
        System.out.println(
                "Min number of words after cleaning in documents: " + minNumWords2);
        System.out.println(
                "Max Number of words after cleaning in documents: " + maxNumWords2);

    }

}
