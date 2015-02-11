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
public interface HTMLfilter {

    static public String filterEN(String dir, String file) {

        File f = new File(dir + file);
        Document html;
        Elements content;

        try {
            html = Jsoup.parse(f, null);
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
            System.out.println("Module filterEN: File " + file + " couldn't load.");
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

    static public ArrayList<String> stopper(ArrayList<String> text, Set<String> stopWordSet, String stopWordDir, String stopWordFile) {

        File f = new File(stopWordDir + stopWordFile);
        try (FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);) {

            if (stopWordSet.isEmpty()) {
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
            System.out.println("Module stopper: File " + stopWordFile + " couldn't load.");
        }

        return null;
    }

}
