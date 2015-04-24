package sri;

import cern.colt.list.tfloat.FloatArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
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
            if (!iF.exists()) {
                IndexedFile iF2 = entryIDs.get(entryIDs.size() - 1);
                while (!iF2.exists()) {
                    entryIDs.remove(iF2.getID());
                    entries.remove(iF2.getChecksum());
                    frequencyIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()).assign(0);
                    weightIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()).assign(0F);
                    fileDictionary.setDirty();
                    if (iF2 == iF) {
                        System.out.println(entryIDs.size());
                        return;
                    }
                    iF2 = entryIDs.get(entryIDs.size() - 1);
                }
                frequencyIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).assign(frequencyIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()));
                weightIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).assign(weightIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()));
                frequencyIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()).assign(0);
                weightIndex.viewPart(iF2.getID(), 0, 1, wordDictionary.size()).assign(0F);
                fileDictionary.replaceAndDelete(iF2.getID(), iF.getID());
            }
        }
    }

    private void processWordDictionary() {
        Iterator<IndexedWord> itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            iW.setDocumentCount(frequencyIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1).cardinality());
            if (iW.getDocumentCount() != 0) {
                iW.doesExist();
            }
        }

        Pair<ArrayList<IndexedWord>, ConcurrentMap<String, Integer>> dictionary = wordDictionary.accessDictionary();

        ArrayList<IndexedWord> entryIDs = dictionary.getKey();
        ConcurrentMap<String, Integer> entries = dictionary.getValue();

        for (int wordID = 0; wordID < entryIDs.size(); wordID++) {
            IndexedWord iW = entryIDs.get(wordID);
            if (!iW.exists()) {
                IndexedWord iW2 = entryIDs.get(entryIDs.size() - 1);
                while (!iW2.exists()) {
                    entryIDs.remove(iW2.getID());
                    entries.remove(iW2.getWord());
                    frequencyIndex.viewPart(0, iW2.getID(), fileDictionary.size(), 1).assign(0);
                    weightIndex.viewPart(0, iW2.getID(), fileDictionary.size(), 1).assign(0F);
                    wordDictionary.setDirty();
                    if (iW2 == iW) {
                        return;
                    }
                    iW2 = entryIDs.get(entryIDs.size() - 1);
                }
                frequencyIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1).assign(frequencyIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1));
                weightIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1).assign(weightIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1));
                frequencyIndex.viewPart(0, iW2.getID(), fileDictionary.size(), 1).assign(0);
                weightIndex.viewPart(0, iW2.getID(), fileDictionary.size(), 1).assign(0F);
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

            Iterator<IndexedWord> itw = wordDictionary.iterator();
            int maxFrequency = frequencyIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).getMaxLocation()[0];
            float normFile = 0F;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float fileFrequency = frequencyIndex.getQuick(iF.getID(), iW.getID()) / (float) maxFrequency;
                float weight = fileFrequency * iW.getIDF();
                normFile += (float) pow(weight, 2);
                weightIndex.setQuick(iF.getID(), iW.getID(), weight);
            }
            normFile = (float) sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float weight = weightIndex.getQuick(iF.getID(), iW.getID());
                if (weight != 0F) {
                    float normWeight = weight / normFile;
                    weightIndex.setQuick(iF.getID(), iW.getID(), normWeight);
                }
            }
        }
    }

    public void generateIndex() {
        ConfigReader configReader = ConfigReader.getInstance();

        if (indexModified) {
            processFileDictionary();
            processWordDictionary();
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

    public void searchResults(ArrayList<String> tokenList) {
        ConfigReader configReader = ConfigReader.getInstance();

        int sizeResult = configReader.getDocumentsRecovered();

        if (sizeResult <= 0) {
            System.out.println("You silly. You asked for no results or even a negative number of them.");
            return;
        }

        if (sizeResult > fileDictionary.size()) {
            sizeResult = fileDictionary.size();
        }

        SparseFloatMatrix2D searchWeight = new SparseFloatMatrix2D(1, wordDictionary.size());
        ArrayList<IndexedWord> searchWords = new ArrayList(tokenList.size());

        Iterator<String> it = tokenList.iterator();

        while (it.hasNext()) {
            String word = it.next();
            int idWord = searchWord(word);
            if (idWord != -1) {
                IndexedWord iW = wordDictionary.search(idWord);
                searchWords.add(iW);
                searchWeight.setQuick(0, idWord, iW.getIDF());
            }
        }

        if (searchWeight.cardinality() == 0) {
            System.out.println("No documents were found.");
            return;
        }

        LinkedList<Pair<IndexedFile, Float>> list = new LinkedList();
        Iterator<IndexedFile> itf = fileDictionary.iterator();

        float minSimilitude = 0F;
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            float similitude = 0F;
            Iterator<IndexedWord> itw = searchWords.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                similitude += weightIndex.getQuick(iF.getID(), iW.getID()) * searchWeight.getQuick(0, iW.getID());
            }
            if (similitude > minSimilitude || list.size() < sizeResult) {
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

        System.out.println("Relevant documents ordered by similitude:");

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

    public int wordQuantity() {
        return wordDictionary.size();
    }

    public int fileQuantity() {
        return fileDictionary.size();
    }

}
