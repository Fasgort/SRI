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
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import javafx.util.Pair;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static volatile DataManager instance = null;
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
            frequencyIndex = new SparseIntMatrix2D(256, 8192);
        } else {
            frequencyIndex = _frequencyIndex;
        }

        if (_weightIndex == null) {
            weightIndex = new SparseFloatMatrix2D(256, 8192);
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

    public int searchWord(String word) {
        int idWord = wordDictionary.search(word);
        if (idWord == -1) {
            if (wordDictionary.size() == frequencyIndex.columns()) {
                resizeIndex(frequencyIndex.rows(), frequencyIndex.columns() * 2);
            }
            IndexedWord iW = new IndexedWord(word);
            idWord = wordDictionary.add(iW);
        }
        return idWord;
    }

    public IndexedWord searchWord(int idWord) {
        return wordDictionary.search(idWord);
    }

    public int searchFile(String file) {
        int idFile = fileDictionary.search(file);
        if (idFile == -1) {
            if (fileDictionary.size() == frequencyIndex.rows()) {
                resizeIndex(frequencyIndex.rows() * 2, frequencyIndex.columns());
            }
            IndexedFile iF = new IndexedFile(file);
            idFile = fileDictionary.add(iF);
        }
        return idFile;
    }

    public IndexedFile searchFile(int idFile) {
        return fileDictionary.search(idFile);
    }

    public boolean checksumFile(int idFile, long checksum) {
        IndexedFile iF = fileDictionary.search(idFile);
        fileDictionary.doesExist(idFile);
        return iF.getChecksum() == checksum;
    }

    public void updateChecksumFile(int idFile, long checksum) {
        IndexedFile iF = fileDictionary.search(idFile);
        fileDictionary.isModified(idFile);
        iF.setChecksum(checksum);
        frequencyIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).assign(0);
        weightIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).assign(0F);
    }

    public void ignoreFile(int idFile) {
        fileDictionary.doesNotExist(idFile);
    }

    public void addFrequency(int idFile, int idWord) {
        int count = frequencyIndex.getQuick(idFile, idWord);
        frequencyIndex.setQuick(idFile, idWord, count + 1);
    }

    public int getFrequency(int idFile, int idWord) {
        return frequencyIndex.getQuick(idFile, idWord);
    }

    private void resizeIndex(int rowSize, int columnSize) {

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

        int bitsetSize = fileDictionary.getBitsetSize();
        int bitsetNewSize = fileDictionary.size();

        BitSet exists = fileDictionary.getExistBitset();
        BitSet updatedExists = new BitSet(fileDictionary.size());

        BitSet modified = fileDictionary.getModifiedBitset();
        BitSet updatedModified = new BitSet(fileDictionary.size());

        updatedExists.or(exists);
        updatedExists.set(bitsetSize, bitsetNewSize);

        updatedModified.or(modified);
        updatedModified.set(bitsetSize, bitsetNewSize);

        int lastOne = updatedExists.previousSetBit(bitsetNewSize - 1);
        int nextOne = updatedExists.nextClearBit(0);

        while (nextOne < lastOne) {

            // Dictionary must be refreshed
            fileDictionary.setDirty();

            frequencyIndex.viewPart(nextOne, 0, 1, wordDictionary.size()).assign(frequencyIndex.viewPart(lastOne, 0, 1, wordDictionary.size()));
            weightIndex.viewPart(nextOne, 0, 1, wordDictionary.size()).assign(weightIndex.viewPart(lastOne, 0, 1, wordDictionary.size()));

            frequencyIndex.viewPart(lastOne, 0, 1, wordDictionary.size()).assign(0);
            weightIndex.viewPart(lastOne, 0, 1, wordDictionary.size()).assign(0F);

            fileDictionary.move(lastOne, nextOne);
            updatedExists.set(nextOne);
            updatedModified.set(nextOne);
            updatedExists.clear(lastOne);

            lastOne = updatedExists.previousSetBit(bitsetNewSize - 1);
            nextOne = updatedExists.nextClearBit(0);

        }

        for (; nextOne < bitsetNewSize; nextOne++) {
            frequencyIndex.viewPart(nextOne, 0, 1, wordDictionary.size()).assign(0);
            weightIndex.viewPart(nextOne, 0, 1, wordDictionary.size()).assign(0F);
        }

        fileDictionary.setExistBitset(updatedExists);
        fileDictionary.setModifiedBitset(updatedModified);
        fileDictionary.setBitsetSize(bitsetNewSize);

    }

    private void processWordDictionary() {

        int bitsetSize = wordDictionary.size();
        BitSet exists = new BitSet(bitsetSize);

        Iterator<IndexedWord> itw = wordDictionary.iterator();

        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            iW.setDocumentCount(frequencyIndex.viewPart(0, iW.getID(), fileDictionary.size(), 1).cardinality());
            if (iW.getDocumentCount() != 0) {
                exists.set(iW.getID());
            }
        }

        int lastOne = exists.previousSetBit(bitsetSize - 1);
        int nextOne = exists.nextClearBit(0);

        while (nextOne < lastOne) {

            // Dictionary must be refreshed
            wordDictionary.setDirty();

            frequencyIndex.viewPart(0, nextOne, fileDictionary.size(), 1).assign(frequencyIndex.viewPart(0, nextOne, fileDictionary.size(), 1));
            weightIndex.viewPart(0, nextOne, fileDictionary.size(), 1).assign(weightIndex.viewPart(0, nextOne, fileDictionary.size(), 1));

            wordDictionary.move(lastOne, nextOne);
            exists.set(nextOne);
            exists.clear(lastOne);

            lastOne = exists.previousSetBit(bitsetSize - 1);
            nextOne = exists.nextClearBit(0);

        }

        wordDictionary.setExistBitset(exists);
        wordDictionary.setBitsetSize(bitsetSize);

    }

    public void generateIndex() {
        ConfigReader configReader = ConfigReader.getInstance();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        processFileDictionary();
        processWordDictionary();

        boolean indexModified = false;

        // Clean removed files
        BitSet existence = fileDictionary.getExistBitset();
        int fileID;
        int count = 0;
        while ((fileID = existence.previousClearBit(fileDictionary.size() - 1 - count)) != -1) {
            count++;
            IndexedFile iF = fileDictionary.search(fileID);

            // File Dictionary must be refreshed
            fileDictionary.setDirty();
            wordDictionary.setDirty();
            indexModified = true;

            // Clean old files
            File deletedFile;
            deletedFile = new File(configReader.getStringDirColEnN() + iF.getFile().replace(".html", ".txt"));
            deletedFile.deleteOnExit();
            deletedFile = new File(configReader.getStringDirColEnStop() + iF.getFile().replace(".html", ".txt"));
            deletedFile.deleteOnExit();
            deletedFile = new File(configReader.getStringDirColEnStem() + iF.getFile().replace(".html", ".txt"));
            deletedFile.deleteOnExit();

        }

        fileDictionary.cleanDictionary();
        wordDictionary.cleanDictionary();

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

        int numberDocuments = fileDictionary.cardinality();

        // Generate IDF & Weight
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            if (!fileDictionary.modified(iF.getID())) {
                continue;
            }

            // Changes must be reflected into the saved files
            fileDictionary.setDirty();
            wordDictionary.setDirty();
            indexModified = true;

            itw = wordDictionary.iterator();
            int maxFrequency = frequencyIndex.viewPart(iF.getID(), 0, 1, wordDictionary.size()).getMaxLocation()[0];
            float normFile = 0F;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int documentsWithWord = iW.getDocumentCount();
                if (documentsWithWord == 0) {
                    iW.setIDF(0F);
                } else {
                    iW.setIDF((float) log((float) numberDocuments / (float) documentsWithWord));
                }
                float fileFrequency = frequencyIndex.getQuick(iF.getID(), iW.getID()) / (float) maxFrequency;
                float weight = fileFrequency * iW.getIDF();
                normFile += pow(weight, 2);
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
            int wordAFrequency = frequencyIndex.viewColumn(wordA.getID()).zSum();
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
                System.out.println("   " + (i + 1) + ": " + result.getKey().getFile() + " with " + result.getValue() + " similitude value.");
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
