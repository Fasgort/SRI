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
import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author Fasgort
 */
public interface HTMLFilter {

    static public Pair<String, String> filterEN(Path filePath) {

        ConfigReader config = ConfigReader.getInstance();

        String including = config.getFilterInclude();
        String excluding = config.getFilterExclude();
        String page = config.getFilterFromPage();

        File f = filePath.toFile();
        Document html;
        Elements content;

        try {
            html = Jsoup.parse(f, null);

            if (including.isEmpty()) {
                if (excluding.isEmpty()) {
                    content = html.getAllElements().not("title");
                } else {
                    content = html.getAllElements().not(excluding + ", title");
                }
            } else {
                if (excluding.isEmpty()) {
                    content = html.select(including);
                } else {
                    content = html.select(including).not(excluding);
                }
            }

            if (!page.isEmpty()) {
                if (!html.select("meta[property=og:url]").attr("content").contains(page)) {
                    return null;
                }
            }

            if (!content.isEmpty()) {
                return new Pair(html.title(), content.text());
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
            m = pt.matcher(word);

            StringBuilder build = new StringBuilder();
            while (m.find()) {
                build.append(m.group());
            }
            newWord = build.toString();

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
