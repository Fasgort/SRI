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
            tokenList = HTMLfilter.normalize(textFiltered);

            try (FileWriter wr = new FileWriter("coleccionEnExtracted/" + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j.toLowerCase() + "\n");
                }
            } catch (Exception e) {
                System.out.println("Failed saving file " + file);
            }

        }

        long end = System.currentTimeMillis();

        System.out.println("Operation was completed in " + (end - start) + " milliseconds.");

    }

}
