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
                
        
        Elements content = html.select("#body p");
        
        System.out.println(html.title());
        System.out.print(content.text());
        
    }
   
}
