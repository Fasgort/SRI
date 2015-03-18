package sri;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tint.impl.SparseIntMatrix2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;
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
    SparseDoubleMatrix2D weightIndex;

    protected DataManager() {
        ConfigReader configReader = ConfigReader.getInstance();

        fileDictionary = FileDictionary.getInstance();
        wordDictionary = WordDictionary.getInstance();

        SparseIntMatrix2D _frequencyIndex = null;
        SparseDoubleMatrix2D _weightIndex = null;

        File serFrequency = new File(configReader.getStringFrequencyIndex());
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

        File serWeight = new File(configReader.getStringWeightIndex());
        if (serWeight.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(serWeight);
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    _weightIndex = (SparseDoubleMatrix2D) ois.readObject();
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
            weightIndex = new SparseDoubleMatrix2D(7500, 300);
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
        return fileDictionary.search(idFile).getChecksum() == checksum;
    }

    public void updateChecksumFile(int idFile, long checksum) {
        fileDictionary.search(idFile).setChecksum(checksum);
    }

    public void addFrequency(int idWord, int idFile) {
        frequencyIndex.setQuick(idWord, idFile, frequencyIndex.getQuick(idWord, idFile) + 1);
    }

    public int getFrequency(int idWord, int idFile) {
        return frequencyIndex.getQuick(idWord, idFile);
    }

    public void generateIndex() {
        int numberDocuments = fileDictionary.size();
        Iterator<IndexedWord> itw;
        Iterator<IndexedFile> itf;

        // Generate IDF
        itw = wordDictionary.iterator();
        while (itw.hasNext()) {
            IndexedWord iW = itw.next();
            int documentsWithWord = frequencyIndex.viewRow(iW.getID()).cardinality();
            iW.setIDF(log((double) numberDocuments / (double) documentsWithWord));
        }

        // Generate Weight
        itf = fileDictionary.iterator();
        while (itf.hasNext()) {
            IndexedFile iF = itf.next();
            itw = wordDictionary.iterator();
            double normFile = 0.0;
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                double fileFrequency = frequencyIndex.getQuick(iW.getID(), iF.getID());
                double weight = fileFrequency * iW.getIDF();
                normFile += pow(weight, 2);
                weightIndex.setQuick(iW.getID(), iF.getID(), weight);
            }
            normFile = sqrt(normFile);
            iF.setNorm(normFile);
            itw = wordDictionary.iterator();
            while (itw.hasNext()) {
                IndexedWord iW = itw.next();
                double normWeight = weightIndex.getQuick(iW.getID(), iF.getID()) / normFile;
                weightIndex.setQuick(iW.getID(), iF.getID(), normWeight);
            }
        }

        try {
            ConfigReader configReader = ConfigReader.getInstance();

            FileOutputStream fos = new FileOutputStream(configReader.getStringFrequencyIndex());
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                frequencyIndex.trimToSize();
                oos.writeObject(frequencyIndex);
            }
        } catch (Exception e) {
            System.out.println("Failed serializing weight table.");
        }

        try {
            ConfigReader configReader = ConfigReader.getInstance();

            FileOutputStream fos = new FileOutputStream(configReader.getStringWeightIndex());
            try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                weightIndex.trimToSize();
                oos.writeObject(weightIndex);
            }
        } catch (Exception e) {
            System.out.println("Failed serializing weight table.");
        }

    }

    public void saveDictionary(String stringDirDictionary) {
        fileDictionary.saveDictionary(stringDirDictionary);
        wordDictionary.saveDictionary(stringDirDictionary);
    }

    public void topFrequentWords(int sizeList) {
        LinkedList<Pair<IndexedWord, Integer>> list = new LinkedList();
        Iterator<IndexedWord> itw = wordDictionary.accessDictionary().iterator();

        int minFrequency = 0;
        while (itw.hasNext()) {
            IndexedWord wordA = itw.next();
            int wordAFrequency = frequencyIndex.viewRow(wordA.getID()).zSum(); // INEFICIENTE
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
