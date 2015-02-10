package sri;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Fasgort
 */
public class HTMLfilter {

    static private Set<String> stopWordSet = null;

    static public String filterEN(String route) {

        File file = new File(route);
        Document html;
        Elements content;

        try {
            html = Jsoup.parse(file, null);
            content = html.select(".post-body > p").not(".read-more");
            if (!html.select("meta[property=og:url]").attr("content").contains("http://www.engadget.com/")) {
                return null;
            }
            if (!content.isEmpty()) {
                return html.title() + "\n" + content.text();
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("File can't load.");
        }

        return null;
    }

    static public ArrayList<String> normalize(String text) {

        ArrayList<String> tokenList = new ArrayList();
        StringTokenizer st = new StringTokenizer(text);
        Pattern pt = Pattern.compile("[\\w]+(['_-]?[\\w]+)*");
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
                tokenList.add(newWord.toLowerCase());
            }
        }

        return tokenList;
    }

    static public ArrayList<String> stopper(ArrayList<String> text) {

        File file = new File("EnglishST.txt");
        try (FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);) {

            if (stopWordSet == null) {
                stopWordSet = new HashSet();
                String word;

                while ((word = br.readLine()) != null) {
                    stopWordSet.add(word);
                }
                br.close();
            }

            ArrayList<String> tokenList = new ArrayList();

            text.stream().filter((j) -> (!stopWordSet.contains(j))).forEach((j) -> {
                tokenList.add(j);
            });

            return tokenList;

        } catch (Exception e) {
            System.out.println("File can't load.");
        }

        return null;
    }

}
