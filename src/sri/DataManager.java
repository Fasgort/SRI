package sri;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix1D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix1D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import javafx.util.Pair;

/**
 *
 * @author Fasgort
 */
public class DataManager {

    private static DataManager instance = null;
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
            try {
                FileInputStream fis = new FileInputStream(serFrequency);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _frequencyIndex = (SparseIntMatrix2D) ois.readObject();
                }
            } catch (Exception e) {
                System.out.println("Failed loading serialized frequency index.");
            }
        }

        File serWeight = new File(configReader.getStringDirIndex() + configReader.getStringWeightIndex());
        if (serWeight.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serWeight);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _weightIndex = (SparseFloatMatrix2D) ois.readObject();
                }
            } catch (Exception e) {
                System.out.println("Failed loading serialized weight index.");
            }
        }

        if (_frequencyIndex == null) {
            frequencyIndex = new SparseIntMatrix2D(7500, 300);
        } else {
            frequencyIndex = _frequencyIndex;
        }

        if (_weightIndex == null) {
            weightIndex = new SparseFloatMatrix2D(7500, 300);
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
        iF.setChecksum(checksum);
        iF.setModified(true);
        frequencyIndex.viewColumn(idFile).assign(0);
        weightIndex.viewColumn(idFile).assign(0F);
    }

    public void ignoreFile(int idFile) {
        fileDictionary.doesNotExist(idFile);
    }

    public void addFrequency(int idWord, int idFile) {
        int count = frequencyIndex.getQuick(idWord, idFile);
        if (count == 0) {
            IndexedWord iW = wordDictionary.search(idWord);
            iW.addDocumentCount();
        }
        frequencyIndex.setQuick(idWord, idFile, count + 1);
    }

    public int getFrequency(int idWord, int idFile) {
        return frequencyIndex.getQuick(idWord, idFile);
    }

    private void trimDictionary() {

        BitSet existence = fileDictionary.getExistence();
        int existence_last = fileDictionary.getExistence_end();

        BitSet updatedExistence = new BitSet(fileDictionary.size());
        int updated_last = fileDictionary.size();

        updatedExistence.flip(0, existence_last);
        updatedExistence.and(existence);
        updatedExistence.flip(existence_last, updated_last);

        int lastOne = updatedExistence.previousSetBit(updated_last - 1);
        int nextOne = updatedExistence.nextClearBit(0);

        while (nextOne < lastOne) {

            SparseIntMatrix1D frequencySwap = new SparseIntMatrix1D(frequencyIndex.viewColumn(nextOne).toArray());
            SparseFloatMatrix1D weightSwap = new SparseFloatMatrix1D(weightIndex.viewColumn(nextOne).toArray());

            frequencyIndex.viewColumn(nextOne).assign(frequencyIndex.viewColumn(lastOne));
            weightIndex.viewColumn(nextOne).assign(weightIndex.viewColumn(lastOne));

            frequencyIndex.viewColumn(lastOne).assign(frequencySwap);
            weightIndex.viewColumn(lastOne).assign(weightSwap);

            fileDictionary.move(lastOne, nextOne);
            updatedExistence.set(nextOne);
            updatedExistence.clear(lastOne);
            lastOne = updatedExistence.previousSetBit(updated_last - 1);
            nextOne = updatedExistence.nextClearBit(0);

        }

        fileDictionary.setExistence(updatedExistence);

    }

    public void generateIndex() {
        ConfigReader configReader = ConfigReader.getInstance();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        trimDictionary();

        // Clean removed files
        BitSet existence = fileDictionary.getExistence();
        int fileID;
        int count = 0;
        while ((fileID = existence.previousClearBit(fileDictionary.size() - 1 - count)) != -1) {
            count++;
            IndexedFile iF = fileDictionary.search(fileID);

            // File Dictionary must be refreshed
            fileDictionary.setDirty();

            // Clean index values related to the file
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int idWord = iW.getID();
                if (frequencyIndex.getQuick(idWord, iF.getID()) != 0) {
                    iW.subDocumentCount();
                }
            }
            frequencyIndex.viewColumn(iF.getID()).assign(0);
            weightIndex.viewColumn(iF.getID()).assign(0F);

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

        int numberDocuments = fileDictionary.existingDocuments();
        boolean indexModified = false;

        // Generate IDF & Weight
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            if (!iF.isModified()) {
                continue;
            }
            indexModified = true;
            itw = wordDictionary.iterator();
            int maxFrequency = frequencyIndex.viewColumn(iF.getID()).getMaxLocation()[0];
            float normFile = 0F;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                int documentsWithWord = iW.getDocumentCount();
                if (documentsWithWord == 0) {
                    iW.setIDF(0F);
                } else {
                    iW.setIDF((float) log((float) numberDocuments / (float) documentsWithWord));
                }
                float fileFrequency = frequencyIndex.getQuick(iW.getID(), iF.getID()) / (float) maxFrequency;
                float weight = fileFrequency * iW.getIDF();
                normFile += pow(weight, 2);
                weightIndex.setQuick(iW.getID(), iF.getID(), weight);
            }
            normFile = (float) sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                float weight = weightIndex.getQuick(iW.getID(), iF.getID());
                if (weight != 0F) {
                    float normWeight = weight / normFile;
                    weightIndex.setQuick(iW.getID(), iF.getID(), normWeight);
                }
            }
        }

        if ("true".equals(configReader.getSerialize()) && indexModified) {

            File indexDir = new File(configReader.getStringDirIndex());
            indexDir.mkdir();

            try {
                FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringFrequencyIndex());
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    frequencyIndex.trimToSize();
                    oos.writeObject(frequencyIndex);
                }
            } catch (Exception e) {
                System.out.println("Failed serializing frequency table.");

            }

            try {
                FileOutputStream fos = new FileOutputStream(configReader.getStringDirIndex() + configReader.getStringWeightIndex());
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    weightIndex.trimToSize();
                    oos.writeObject(weightIndex);
                }
            } catch (Exception e) {
                System.out.println("Failed serializing weight table.");

            }

        }

    }

    public void saveDictionary() {
        fileDictionary.saveDictionary();
        wordDictionary.saveDictionary();
    }

    public void topFrequentWords(int sizeList) {
        LinkedList<Pair<IndexedWord, Integer>> list = new LinkedList();
        Iterator<IndexedWord> itw = wordDictionary.accessDictionary().iterator();

        int minFrequency = 0;
        while (itw.hasNext()) {
            IndexedWord wordA = itw.next();
            int wordAFrequency = frequencyIndex.viewRow(wordA.getID()).zSum();
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

        for (int i = 0; i < sizeList; i++) {
            Pair<IndexedWord, Integer> word = list.removeFirst();
            System.out.println("   " + word.getKey().getWord() + " with " + word.getValue() + " apparitions in documents.");
        }

    }

    public int wordQuantity() {
        return wordDictionary.size();
    }

    public int fileQuantity() {
        return fileDictionary.size();
    }

}
