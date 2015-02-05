package sri;

import java.io.*;

/**
 *
 * @author Fasgort
 */
public class SRI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String textFiltered = HTMLfilter.filterEN("coleccionEN/en002.html");
        HTMLfilter.normalize(textFiltered);
    }

}
