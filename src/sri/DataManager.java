package sri;

import cern.colt.list.tfloat.FloatArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.colt.matrix.tint.IntMatrix1D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentMap;
import javafx.util.Pair;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static volatile DataManager instance = null;
    private static transient boolean indexModified = false;
    final private FileDictionary fileDictionary;
    final private WordDictionary wordDictionary;
    SparseIntMatrix2D frequencyIndex;
    SparseFloatMatrix2D weightIndex;

    protected DataManager() {
        ConfigReader configReader = ConfigReader.getInstance();

        fileDictionary = FileDictionary.getInstance();
        wordDictionary = WordDictionary.getInstance();

        SparseIntMatrix2D _frequencyIndex = null;
        SparseFloatMatrix2D _weightIndex = null;

        File serFrequency = new File(configReader.getStringDirIndex() + configReader.getStringFrequencyIndex());
        if (serFrequency.canRead()) {
            try (FileInputStream fis = new FileInputStream(serFrequency);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                _frequencyIndex = (SparseIntMatrix2D) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }

        File serWeight = new File(configReader.getStringDirIndex() + configReader.getStringWeightIndex());
        if (serWeight.canRead()) {
            try (FileInputStream fis = new FileInputStream(serWeight);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                _weightIndex = (SparseFloatMatrix2D) ois.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println(ex);
            }
        }

        if (_frequencyIndex == null) {
            frequencyIndex = new SparseIntMatrix2D(1024, 16384);
        } else {
            frequencyIndex = _frequencyIndex;
        }

        if (_weightIndex == null) {
            weightIndex = new SparseFloatMatrix2D(1024, 16384);
        } else {
            weightIndex = _weightIndex;
        }

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public int searchOrAddWord(String word) {
        int idWord = wordDictionary.search(word);
        if (idWord == -1) {
            synchronized (this) {
                if (wordDictionary.size() == frequencyIndex.columns()) {
                    resizeIndex(frequencyIndex.rows(), frequencyIndex.columns() * 2);
                }
                IndexedWord iW = new IndexedWord(word);
                idWord = wordDictionary.add(iW);
                indexModified = true;
            }
        }
        return idWord;
    }

    public int searchWord(String word) {
        return wordDictionary.search(word);
    }

    public IndexedWord searchWord(int idWord) {
        return wordDictionary.search(idWord);
    }

    public int searchOrAddFile(long checksum) {
        int idFile = fileDictionary.search(checksum);
        if (idFile == -1) {
            synchronized (this) {
                if (fileDictionary.size() == frequencyIndex.rows()) {
                    resizeIndex(frequencyIndex.rows() * 2, frequencyIndex.columns());
                }
                IndexedFile iF = new IndexedFile(checksum);
                idFile = fileDictionary.add(iF);
                indexModified = true;
            }
        }
        return idFile;
    }

    public IndexedFile searchFile(int idFile) {
        return fileDictionary.search(idFile);
    }

    public boolean isFileNew(int idFile) {
        return fileDictionary.isNew(idFile);
    }

    public boolean isFileNamed(int idFile) {
        return fileDictionary.isNamed(idFile);
    }

    public void nameFile(int idFile, String file) {
        IndexedFile iF = fileDictionary.search(idFile);
        iF.setFile(file);
        fileDictionary.doesExist(idFile);
    }

    public void updateFile(int idFile, String file, String title) {
        IndexedFile iF = fileDictionary.search(idFile);
        iF.setFile(file);
        iF.setTitle(title);
        fileDictionary.doesExist(idFile);
    }

    public void ignoreFile(int idFile) {
        fileDictionary.doesNotExist(idFile);
    }

    public synchronized void addFrequency(int idFile, int idWord) {
        searchWord(idWord).sumFrequency(1);
        int count = frequencyIndex.getQuick(idFile, idWord);
        frequencyIndex.setQuick(idFile, idWord, count + 1);
    }

    private void resizeIndex(int rowSize, int columnSize) {

        indexModified = true;

        if (rowSize == 0) {
            rowSize = 1;
        }
        if (columnSize == 0) {
            columnSize = 1;
        }

        // Frequency Index
        {
            SparseIntMatrix2D _frequencyIndex = (SparseIntMatrix2D) frequencyIndex.like(rowSize, columnSize);

            int cardinality = frequencyIndex.viewPart(0, 0, fileDictionary.size(), wordDictionary.size()).cardinality();

            if (cardinality != 0) {
                IntArrayList rows = new IntArrayList(cardinality);
                IntArrayList columns = new IntArrayList(cardinality);
                IntArrayList values = new IntArrayList(cardinality);
                frequencyIndex.viewPart(0, 0, fileDictionary.size(), wordDictionary.size()).getNonZeros(rows, columns, values);

                for (int i = 0; i < cardinality; i++) {
                    _frequencyIndex.setQuick(rows.getQuick(i), columns.getQuick(i), values.getQuick(i));
                }
            }

            frequencyIndex = _frequencyIndex;
        }

        // Weight Index
        {
            SparseFloatMatrix2D _weightIndex = (SparseFloatMatrix2D) weightIndex.like(rowSize, columnSize);

            int cardinality = weightIndex.viewPart(0, 0, fileDictionary.size(), wordDictionary.size()).cardinality();

            if (cardinality != 0) {
                IntArrayList rows = new IntArrayList(cardinality);
                IntArrayList columns = new IntArrayList(cardinality);
                FloatArrayList values = new FloatArrayList(cardinality);
                weightIndex.viewPart(0, 0, fileDictionary.size(), wordDictionary.size()).getNonZeros(rows, columns, values);

                for (int i = 0; i < cardinality; i++) {
                    _weightIndex.setQuick(rows.getQuick(i), columns.getQuick(i), values.getQuick(i));
                }
            }

            weightIndex = _weightIndex;
        }

    }

    private void processFileDictionary() {

        Pair<ArrayList<IndexedFile>, ConcurrentMap<Long, Integer>> dictionary = fileDictionary.accessDictionary();

        ArrayList<IndexedFile> entryIDs = dictionary.getKey();
        ConcurrentMap<Long, Integer> entries = dictionary.getValue();

        for (int fileID = 0; fileID < entryIDs.size(); fileID++) {
            IndexedFile iF = entryIDs.get(fileID);
            IntMatrix1D viewFrequencyFileColumn = frequencyIndex.viewRow(iF.getID());
            FloatMatrix1D viewWeightFileColumn = weightIndex.viewRow(iF.getID());
            if (!iF.exists()) {
                indexModified = true;
                IndexedFile iF2 = entryIDs.get(entryIDs.size() - 1);
                IntMatrix1D viewFrequencyFileColumn2 = frequencyIndex.viewRow(iF2.getID());
                FloatMatrix1D viewWeightFileColumn2 = weightIndex.viewRow(iF2.getID());
                while (!iF2.exists()) {
                    entryIDs.remove(iF2.getID());
                    entries.remove(iF2.getChecksum());
                    for (int i = 0; i < wordDictionary.size(); i++) {
                        int frequency = frequencyIndex.getQuick(iF2.getID(), i);
                        if (frequency != 0) {
                            viewFrequencyFileColumn2.setQuick(i, 0);
                            viewWeightFileColumn2.setQuick(i, 0F);
                            searchWord(i).lessFrequency(frequency);
                        }
                    }
                    fileDictionary.setDirty();

                    if (iF2 == iF) {
                        return;
                    }
                    iF2 = entryIDs.get(entryIDs.size() - 1);
                    viewFrequencyFileColumn2 = frequencyIndex.viewRow(iF2.getID());
                    viewWeightFileColumn2 = weightIndex.viewRow(iF2.getID());
                }
                for (int i = 0; i < wordDictionary.size(); i++) {
                    int frequency = viewFrequencyFileColumn.getQuick(i);
                    if (frequency != 0) {
                        searchWord(i).lessFrequency(frequency);
                    }
                }
                viewFrequencyFileColumn.viewPart(0, wordDictionary.size()).assign(viewFrequencyFileColumn2.viewPart(0, wordDictionary.size()));
                viewWeightFileColumn.viewPart(0, wordDictionary.size()).assign(viewWeightFileColumn2.viewPart(0, wordDictionary.size()));
                viewFrequencyFileColumn2.viewPart(0, wordDictionary.size()).assign(0);
                viewWeightFileColumn2.viewPart(0, wordDictionary.size()).assign(0F);
                fileDictionary.replaceAndDelete(iF2.getID(), iF.getID());
            }
        }
    }

    private void processWordDictionary() {
        Iterator<IndexedWord> itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            IntMatrix1D viewFrequencyWordColumn = frequencyIndex.viewColumn(iW.getID());
            iW.setDocumentCount(viewFrequencyWordColumn.viewPart(0, fileDictionary.size()).cardinality());
            if (iW.getDocumentCount() != 0) {
                iW.doesExist();
            }
        }

        Pair<ArrayList<IndexedWord>, ConcurrentMap<String, Integer>> dictionary = wordDictionary.accessDictionary();

        ArrayList<IndexedWord> entryIDs = dictionary.getKey();
        ConcurrentMap<String, Integer> entries = dictionary.getValue();

        for (int wordID = 0; wordID < entryIDs.size(); wordID++) {
            IndexedWord iW = entryIDs.get(wordID);
            IntMatrix1D viewFrequencyWordColumn = frequencyIndex.viewColumn(iW.getID());
            FloatMatrix1D viewWeightWordColumn = weightIndex.viewColumn(iW.getID());
            if (!iW.exists()) {
                indexModified = true;
                IndexedWord iW2 = entryIDs.get(entryIDs.size() - 1);
                IntMatrix1D viewFrequencyWordColumn2 = frequencyIndex.viewColumn(iW2.getID());
                FloatMatrix1D viewWeightWordColumn2 = weightIndex.viewColumn(iW2.getID());
                while (!iW2.exists()) {
                    entryIDs.remove(iW2.getID());
                    entries.remove(iW2.getWord());
                    viewFrequencyWordColumn2.viewPart(0, fileDictionary.size()).assign(0);
                    viewWeightWordColumn2.viewPart(0, fileDictionary.size()).assign(0F);
                    wordDictionary.setDirty();
                    if (iW2 == iW) {
                        return;
                    }
                    iW2 = entryIDs.get(entryIDs.size() - 1);
                    viewFrequencyWordColumn2 = frequencyIndex.viewColumn(iW2.getID());
                    viewWeightWordColumn2 = weightIndex.viewColumn(iW2.getID());
                }
                viewFrequencyWordColumn.viewPart(0, fileDictionary.size()).assign(viewFrequencyWordColumn2.viewPart(0, fileDictionary.size()));
                viewWeightWordColumn.viewPart(0, fileDictionary.size()).assign(viewWeightWordColumn2.viewPart(0, fileDictionary.size()));
                viewFrequencyWordColumn2.viewPart(0, fileDictionary.size()).assign(0);
                viewWeightWordColumn2.viewPart(0, fileDictionary.size()).assign(0F);
                wordDictionary.replaceAndDelete(iW2.getID(), iW.getID());
            }
        }

    }

    public void updateMatrixSize() {
        if (fileDictionary.size() * 3 < frequencyIndex.rows() && wordDictionary.size() * 3 < frequencyIndex.columns()) {
            resizeIndex(fileDictionary.size() * 2, wordDictionary.size() * 2);
        } else {
            if (fileDictionary.size() * 3 < frequencyIndex.rows()) {
                resizeIndex((int) (fileDictionary.size() * 1.5), frequencyIndex.columns());
            }
            if (wordDictionary.size() * 3 < frequencyIndex.columns()) {
                resizeIndex(frequencyIndex.rows(), (int) (wordDictionary.size() * 1.5));
            }
        }
    }

    public void generateIDF() {
        int numberDocuments = fileDictionary.size();

        if (wordDictionary.isDirty()) {
            Iterator<IndexedWord> itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int documentsWithWord = iW.getDocumentCount();
                if (documentsWithWord == 0) {
                    iW.setIDF(0F);
                } else {
                    iW.setIDF((float) log((float) numberDocuments / (float) documentsWithWord));
                }

            }
            // As IDF changed, documents must be refreshed
            fileDictionary.setDirty();
        }

    }

    public void generateWeight() {
        Iterator<IndexedFile> itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            IntMatrix1D viewFrequencyFileRow = frequencyIndex.viewRow(iF.getID());
            FloatMatrix1D viewWeightFileRow = weightIndex.viewRow(iF.getID());

            Iterator<IndexedWord> itw = wordDictionary.iterator();
            int maxFrequency = viewFrequencyFileRow.viewPart(0, wordDictionary.size()).getMaxLocation()[0];
            float normFile = 0F;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float fileFrequency = viewFrequencyFileRow.getQuick(iW.getID()) / (float) maxFrequency;
                float weight = fileFrequency * iW.getIDF();
                if (weight == 0F) {
                    continue;
                }
                normFile += pow(weight, 2);
                viewWeightFileRow.setQuick(iW.getID(), weight);
            }
            normFile = (float) sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float weight = viewWeightFileRow.getQuick(iW.getID());
                if (weight != 0F) {
                    float normWeight = weight / normFile;
                    viewWeightFileRow.setQuick(iW.getID(), normWeight);
                }
            }
        }
    }

    public void generateIndex() {
        ConfigReader configReader = ConfigReader.getInstance();

        processFileDictionary();
        processWordDictionary();

        if (indexModified) {
            updateMatrixSize();
            generateIDF();
            generateWeight();
        }

        if (configReader.getSerialize() && indexModified) {

            File indexDir = new File(configReader.getStringDirIndex());
            indexDir.mkdir();

            try (FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringFrequencyIndex());
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                frequencyIndex.trimToSize();
                oos.writeObject(frequencyIndex);
            } catch (IOException ex) {
                System.err.println(ex);
            }

            try (FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringWeightIndex());
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                weightIndex.trimToSize();
                oos.writeObject(weightIndex);
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }

    }

    public void saveDictionary() {
        fileDictionary.saveDictionary();
        wordDictionary.saveDictionary();
    }

    public void topFrequentWords(int sizeList) {

        if (sizeList > wordDictionary.size()) {
            sizeList = wordDictionary.size();
        }

        if (sizeList <= 0) {
            return;
        }

        LinkedList<Pair<IndexedWord, Integer>> list = new LinkedList();
        Iterator<IndexedWord> itw = wordDictionary.iterator();

        int minFrequency = 0;
        while (itw.hasNext()) {
            IndexedWord wordA = itw.next();
            int wordAFrequency = wordA.getFrequency();
            if (wordAFrequency > minFrequency || list.size() < sizeList) {
                if (list.size() == 0) {
                    list.add(new Pair(wordA, wordAFrequency));
                } else {
                    Iterator<Pair<IndexedWord, Integer>> itList = list.descendingIterator();
                    int i = list.size();
                    boolean added = false;
                    while (itList.hasNext()) {
                        int wordBFrequency = itList.next().getValue();
                        if (wordAFrequency <= wordBFrequency) {
                            list.add(i, new Pair(wordA, wordAFrequency));
                            added = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                    if (!added) {
                        list.addFirst(new Pair(wordA, wordAFrequency));
                    }
                }
                if (list.size() > sizeList) {
                    minFrequency = list.getLast().getValue();
                    list.removeLast();
                }
            }
        }

        System.out.println("Top " + sizeList + " frequent words:");

        for (int i = 0; i < sizeList; i++) {
            Pair<IndexedWord, Integer> word = list.removeFirst();
            System.out.println("   " + word.getKey().getWord() + " with " + word.getValue() + " apparitions in documents.");
        }

    }

    public void searchResults(String searchString, ArrayList<String> tokenList) {
        ConfigReader configReader = ConfigReader.getInstance();

        int sizeResult = configReader.getDocumentsRecovered();

        if (sizeResult <= 0) {
            sizeResult = 5;
        }

        if (sizeResult > fileDictionary.size()) {
            sizeResult = fileDictionary.size();
        }

        SparseFloatMatrix1D searchWeight = new SparseFloatMatrix1D(wordDictionary.size());
        ArrayList<IndexedWord> searchWords = new ArrayList(tokenList.size());

        Iterator<String> it = tokenList.iterator();
        float normSearch = 0F;

        while (it.hasNext()) {
            String word = it.next();
            int idWord = searchWord(word);
            if (idWord != -1) {
                IndexedWord iW = wordDictionary.search(idWord);
                searchWords.add(iW);
                searchWeight.setQuick(idWord, iW.getIDF());
                normSearch += pow(iW.getIDF(), 2);
            }
        }
        normSearch = (float) sqrt(normSearch);
        for (IndexedWord iW : searchWords) {
            float weight = searchWeight.getQuick(iW.getID());
            if (weight != 0F) {
                float normWeight = weight / normSearch;
                searchWeight.setQuick(iW.getID(), normWeight);
            }
        }

        if (searchWeight.cardinality() == 0) {
            synchronized (this) {
                System.out.println("Search input was: \"" + searchString + "\"");
                System.out.println("No documents were found.");
                System.out.println();
            }
            return;
        }

        LinkedList<Pair<IndexedFile, Float>> list = new LinkedList();
        Iterator<IndexedFile> itf = fileDictionary.iterator();

        float minSimilitude = configReader.getMinSimilitude();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            FloatMatrix1D viewRow = weightIndex.viewRow(iF.getID());
            float weightIndexed;
            float weightSearched;
            float similitude = 0F;
            float normFile = iF.getNorm();
            Iterator<IndexedWord> itw = searchWords.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                weightIndexed = viewRow.getQuick(iW.getID());
                weightSearched = searchWeight.getQuick(iW.getID());
                similitude += weightIndexed * weightSearched;
            }

            if (similitude == 0F) {
                continue;
            }

            similitude = similitude / (normFile * normSearch);

            if (similitude > minSimilitude) {
                if (list.size() == 0) {
                    list.add(new Pair(iF, similitude));
                } else {
                    Iterator<Pair<IndexedFile, Float>> itList = list.descendingIterator();
                    int i = list.size();
                    boolean added = false;
                    while (itList.hasNext()) {
                        float otherSimilitude = itList.next().getValue();
                        if (similitude <= otherSimilitude) {
                            list.add(i, new Pair(iF, similitude));
                            added = true;
                            break;
                        } else {
                            i--;
                        }
                    }
                    if (!added) {
                        list.addFirst(new Pair(iF, similitude));
                    }
                }
                if (list.size() > sizeResult) {
                    minSimilitude = list.getLast().getValue();
                    list.removeLast();
                }
            }
        }

        if (list.size() == 0) {
            synchronized (this) {
                System.out.println("Search input was: \"" + searchString + "\"");
                System.out.println("No documents were found.");
                System.out.println();
            }
            return;
        }

        synchronized (this) {
            System.out.println("Search input was: \"" + searchString + "\"");

            System.out.println("Relevant documents ordered by similitude:");

            sizeResult = list.size();

            for (int i = 0; i < sizeResult; i++) {
                Pair<IndexedFile, Float> result = list.removeFirst();
                if (result.getValue() != 0F) {
                    System.out.println("    " + (i + 1) + ": " + result.getKey().getTitle());
                    System.out.println("    " + "File is named \"" + result.getKey().getFile() + "\" and has \"" + result.getValue() + "\" similitude value.");
                    System.out.println();
                }
            }
            System.out.println();
        }

    }

    public int wordQuantity() {
        return wordDictionary.size();
    }

    public int fileQuantity() {
        return fileDictionary.size();
    }

}
