package sri;

import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Fasgort
 */
public class SearchThread implements Runnable {

    private final Set<String> stopWordSet;
    private String searchString;

    public SearchThread(Set<String> stopWord, String search) {
        stopWordSet = stopWord;
        searchString = search;
    }

    @Override
    public void run() {

        ConfigReader configReader = ConfigReader.getInstance();
        DataManager dataManager = DataManager.getInstance();

        ArrayList<String> tokenList;

        // Módulo Normalize
        tokenList = HTMLFilter.normalize(searchString);

        // Módulo Stopper
        tokenList = HTMLFilter.stopper(tokenList, stopWordSet, configReader.getDirResources(), configReader.getStopWordFilename());

        // Módulo Stemmer
        tokenList = HTMLFilter.stemmer(tokenList);

        // Búsqueda
        dataManager.searchResults(searchString, tokenList);
    }
}
