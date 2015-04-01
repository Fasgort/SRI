package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author Fasgort
 */
public interface HTMLfilter {

    static public String filterEN(Path filePath) {

        File f = filePath.toFile();
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
        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }

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

        } catch (IOException ex) {
            System.err.println(ex);
            return null;
        }

    }

    static public ArrayList<String> stemmer(ArrayList<String> wordArray) {

        ArrayList<String> stemmedWords = new ArrayList();

        try {
            Class stemClass = Class.forName("org.tartarus.snowball.ext."
                    + "englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

            wordArray.stream().map((j) -> {
                stemmer.setCurrent(j);
                return j;
            }).forEach((j) -> {
                if (stemmer.stem()) {
                    stemmedWords.add(stemmer.getCurrent());
                } else {
                    stemmedWords.add(j);
                }
            });

            return stemmedWords;

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.err.println(ex);
            return null;
        }

    }

}
