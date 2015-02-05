package sri;

import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Fasgort
 */
public class HTMLfilter {

    static public String filterEN(String route) {

        File file = new File(route);
        Document html = null;

        try {
            html = Jsoup.parse(file, null);
        } catch (Exception e) {
            System.out.println("File can't load.");
        }

        Elements content = html.select(".post-body p").not(".read-more");

        return html.title() + content.text();
    }

    static public ArrayList<String> normalize(String text) {

        ArrayList<String> tokenList = new ArrayList();
        StringTokenizer st = new StringTokenizer(text);
        Pattern pt = Pattern.compile("[a-zA-Z0-9'_-]*");
        Matcher m;
        String word;
        String newWord;

        while (st.hasMoreTokens()) {
            word = st.nextToken();
            newWord = "";
            m = pt.matcher(word);

            while (m.find()) {
                newWord += m.group();
            }

            if (!newWord.isEmpty()) {
                tokenList.add(newWord);
            }
        }

        return tokenList;
    }

}
