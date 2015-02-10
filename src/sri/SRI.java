package sri;

import java.io.*;
import java.util.ArrayList;

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
        
        long start = System.currentTimeMillis();

        for (int i = 1; i <= 200; i++) {

            ArrayList<String> tokenList;
            String dir = "coleccionEN/";
            String file;

            file = "en";
            if (i < 100) {
                file += "0";
            }
            if (i < 10) {
                file += "0";
            }
            file += i;
            file += ".html";

            String textFiltered = HTMLfilter.filterEN(dir + file);
            if(textFiltered == null) continue;

            tokenList = HTMLfilter.normalize(textFiltered);
            File dirNorm = new File("coleccionEnNormalized/");
            dirNorm.mkdir();
            try (FileWriter wr = new FileWriter("coleccionEnNormalized/" + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                }
            } catch (Exception e) {
                System.out.println("Failed saving file " + file);
            }
            numWords += tokenList.size();
            if(tokenList.size() > maxNumWords) maxNumWords = tokenList.size();
            if(tokenList.size() < minNumWords) minNumWords = tokenList.size();
            

            tokenList = HTMLfilter.stopper(tokenList);
            if(tokenList == null) continue;
            File dirStop = new File("coleccionEnStopped/");
            dirStop.mkdir();
            try (FileWriter wr = new FileWriter("coleccionEnStopped/" + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                }
            } catch (Exception e) {
                System.out.println("Failed saving file " + file);
            }
            numWords2 += tokenList.size();
            if(tokenList.size() > maxNumWords2) maxNumWords2 = tokenList.size();
            if(tokenList.size() < minNumWords2) minNumWords2 = tokenList.size();

        }

        long end = System.currentTimeMillis();

        System.out.println("Operation was completed in " + (end - start) + " milliseconds.");
        
        System.out.println("Number of words before cleaning: " + numWords);
        System.out.println("Average words before cleaning: " + numWords/200);
        System.out.println("Min number of words before cleaning in documents: " + minNumWords);
        System.out.println("Max Number of words before cleaning in documents: " + maxNumWords);
        
        
        
        System.out.println("Number of words after cleaning: " + numWords2);
        System.out.println("Average words after cleaning: " + numWords2/200);
        System.out.println("Min number of words after cleaning in documents: " + minNumWords2);
        System.out.println("Max Number of words after cleaning in documents: " + maxNumWords2);

    }

}
