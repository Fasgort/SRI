package sri;

import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Fasgort
 */
public class HTMLfilter {

    static public void filter(String rutaFichero) {

        File file = new File(rutaFichero);
        Document html = null;

        try {
            html = Jsoup.parse(file, null);
        } catch (Exception e) {
            System.out.println("El fichero no existe.");
        }

        Elements content = html.select(".post-body p").not(".read-more");

        System.out.println(html.title());
        System.out.println(content.text());
        
        System.out.println(rutaFichero.replace("*/", ""));

        try {
            FileWriter wr = new FileWriter("coleccionEnExtracted/" + rutaFichero.replace("coleccionEN/", ""));
            wr.write(html.title() + "\n" + content.text());
            wr.close();
        } catch (Exception e) {
            System.out.println("El texto extraído no se pudo guardar.");
        }

    }

}
