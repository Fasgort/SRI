package sri;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author Fasgort
 */
public class SRI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String debug = "false";
        String dirResources = null;
        String stopWordFilename = null;
        String stringDirColEn = null;
        String stringDirColEnN = null;
        String stringDirColEnStop = null;
        String stringDirColEnStem = null;

        // Lectura de configuración
        File confData = new File("./conf.data");

        try (FileReader fr = new FileReader(confData);
                BufferedReader br = new BufferedReader(fr);) {

            Pattern comment = Pattern.compile("^[\\w]+ = [\\w/.]+");
            Matcher m;
            String linea;

            while ((linea = br.readLine()) != null) {
                m = comment.matcher(linea);

                if (m.find()) {
                    linea = m.group();

                    String atributo;
                    String valor;

                    Pattern atribP = Pattern.compile("^[\\w]+ ={0}");
                    m = atribP.matcher(linea);
                    if (m.find()) {
                        atributo = m.group().trim();
                    } else {
                        continue;
                    }

                    Pattern atribV = Pattern.compile("={0} [\\w/.]+");
                    m = atribV.matcher(linea);
                    if (m.find()) {
                        valor = m.group().trim();
                    } else {
                        continue;
                    }

                    switch (atributo) {
                        case "debug":
                            debug = valor;
                            break;
                        case "dirResources":
                            dirResources = valor;
                            break;
                        case "stopWordFilename":
                            stopWordFilename = valor;
                            break;
                        case "stringDirColEn":
                            stringDirColEn = valor;
                            break;
                        case "stringDirColEnN":
                            stringDirColEnN = valor;
                            break;
                        case "stringDirColEnStop":
                            stringDirColEnStop = valor;
                            break;
                        case "stringDirColEnStem":
                            stringDirColEnStem = valor;
                            break;
                    }

                }

            }

            br.close();

        } catch (Exception e) {
            System.out.println("Config file couldn't load. It must be included with the executable.");
            return;
        }

        if (dirResources == null || stopWordFilename == null
                || stringDirColEn == null || stringDirColEnN == null
                || stringDirColEnStop == null || stringDirColEnStem == null) {
            System.out.println("Config file syntax is wrong. SRI can't load correctly.");
            return;
        }
        // Fin Lectura de configuración

        int numWords = 0;
        int minNumWords = Integer.MAX_VALUE;
        int maxNumWords = Integer.MIN_VALUE;

        int numWords2 = 0;
        int minNumWords2 = Integer.MAX_VALUE;
        int maxNumWords2 = Integer.MIN_VALUE;

        // DATA STRUCTURES
        Set<String> stopWordSet = new HashSet(800); // Stop Word Dictionary
        ArrayList<String> wordDictionary = new ArrayList(10000); // Word Dictionary ID -> word
        Map<String, Integer> idWordDictionary = new HashMap(30000); // Word Dictionary word -> ID
        ArrayList<String> fileDictionary = new ArrayList(200); // File Dictionary ID -> file
        Map<String, Integer> idFileDictionary = new HashMap(300); // File Dictionary file -> ID
        ArrayList<Map<Integer, FileFrequency>> wordFrequency = new ArrayList(10000); // Main Data Structure for word frequency
        Map<Integer, WordFrequency> stemmedWords = new HashMap(20000); //Temporary. Being only used for stats.

        // Lectura de directorio
        File dirHTML = new File(stringDirColEn);
        dirHTML.mkdir();

        //Lectura de ficheros
        File[] arrayHTMLfile = dirHTML.listFiles();

        // No existen ficheros en el directorio
        if (arrayHTMLfile.length == 0) {
            System.out.println("No files to filter. Exiting program.");
            return;
        }

        // Inicio de operaciones
        long start = System.currentTimeMillis();

        // Filtrado HTML
        for (File arrayHTMLfile1 : arrayHTMLfile) {

            ArrayList<String> tokenList;

            String file = arrayHTMLfile1.getName();
            Integer idFile = idFileDictionary.get(file);
            String textFiltered = HTMLfilter.filterEN(stringDirColEn, file);
            if (textFiltered == null) {
                if (debug.contentEquals("true")) {
                    System.out.println("File " + file + " was ignored and won't be included in the SE.");
                }
                continue;
            }
            if (idFile == null) {
                IndexedFile newFile = new IndexedFile(file);
                idFile = newFile.getID();
                idFileDictionary.put(file, idFile);
                fileDictionary.add(idFile, file);
            }

            tokenList = HTMLfilter.normalize(textFiltered);

            File dirNorm = new File(stringDirColEnN);
            dirNorm.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnN + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                }
            } catch (Exception e) {
                System.out.println("Failed saving normalised file " + file);
            }

            numWords += tokenList.size();
            if (tokenList.size() > maxNumWords) {
                maxNumWords = tokenList.size();
            }
            if (tokenList.size() < minNumWords) {
                minNumWords = tokenList.size();
            }
            // Fin Filtrado HTML

            // Módulo Stopper
            tokenList = HTMLfilter.stopper(tokenList, stopWordSet, dirResources, stopWordFilename);
            if (tokenList == null) {
                continue;
            }

            File dirStop = new File(stringDirColEnStop);
            dirStop.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnStop + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                }
            } catch (Exception e) {
                System.out.println("Failed saving cleaned file " + file);
            }

            numWords2 += tokenList.size();
            if (tokenList.size() > maxNumWords2) {
                maxNumWords2 = tokenList.size();
            }
            if (tokenList.size() < minNumWords2) {
                minNumWords2 = tokenList.size();
            }
            // Fin Módulo Stopper

            // Módulo Stemmer
            tokenList = HTMLfilter.stemmer(tokenList);
            if (tokenList == null) {
                continue;
            }

            File dirStem = new File(stringDirColEnStem);
            dirStem.mkdir();
            try (FileWriter wr = new FileWriter(stringDirColEnStem + file.replace(".html", ".txt"))) {
                for (String j : tokenList) {
                    wr.write(j + "\n");
                    Integer idWord = idWordDictionary.get(j);

                    // Agregación de palabras al diccionario
                    if (idWord == null) {
                        IndexedWord newWord = new IndexedWord(j);
                        idWord = newWord.getID();
                        idWordDictionary.put(j, idWord);
                        wordDictionary.add(idWord, j);
                        wordFrequency.add(idWord, new HashMap(10000));
                    }

                    // Actualizamos frecuencia de la palabra en el documento
                    FileFrequency ff;
                    ff = wordFrequency.get(idWord).get(idFile);
                    if (ff == null) {
                        wordFrequency.get(idWord).put(idFile, new FileFrequency(idFile));
                    } else {
                        ff.addCount();
                    }

                    // Actualizamos frequencia de la palabra (temporal, usado sólo para estadísticas)
                    WordFrequency fw;
                    fw = stemmedWords.get(idWord);
                    if (fw == null) {
                        stemmedWords.put(idWord, new WordFrequency(idWord));
                    } else {
                        fw.addCount();
                    }

                }
            } catch (Exception e) {
                System.out.println("Failed saving stemmed file " + file);
            }
            // Fin Módulo Stemmer

        }

        // Generación de listas de palabras frecuentes
        Collection<WordFrequency> collectStemmed = stemmedWords.values();
        Iterator<WordFrequency> its = collectStemmed.iterator();
        LinkedList<WordFrequency> nfrequentStemmedWords = new LinkedList();

        int minS = 0;
        while (its.hasNext()) {
            WordFrequency fw = its.next();
            if (fw.getCount() > minS || nfrequentStemmedWords.size() < 5) {
                if (nfrequentStemmedWords.size() == 0) {
                    nfrequentStemmedWords.add(fw);
                } else {
                    Iterator<WordFrequency> nsw = nfrequentStemmedWords.descendingIterator();
                    int index = nfrequentStemmedWords.size();
                    boolean added = false;
                    while (nsw.hasNext()) {
                        WordFrequency fw2 = nsw.next();
                        if (fw.getCount() <= fw2.getCount()) {
                            nfrequentStemmedWords.add(index, fw);
                            added = true;
                            break;
                        } else {
                            index--;
                        }
                    }
                    if (!added) {
                        nfrequentStemmedWords.addFirst(fw);
                    }
                }
                if (nfrequentStemmedWords.size() > 5) {
                    minS = nfrequentStemmedWords.getLast().getCount();
                    nfrequentStemmedWords.removeLast();
                }
            }
        }

        // Fin de operaciónes
        long end = System.currentTimeMillis();

        // Estadísticas
        WordFrequency fw;

        System.out.println(
                "Operation was completed in " + (end - start) + " milliseconds.");
        System.out.println();

        System.out.println(
                "Number of words after filtering: " + numWords);
        System.out.println(
                "Average words after filtering: " + numWords / arrayHTMLfile.length);
        System.out.println(
                "Min number of words after filtering in documents: " + minNumWords);
        System.out.println(
                "Max Number of words after filtering in documents: " + maxNumWords);
        System.out.println();

        System.out.println(
                "Number of words after cleaning: " + numWords2);
        System.out.println(
                "Average words after cleaning: " + numWords2 / arrayHTMLfile.length);
        System.out.println(
                "Min number of words after cleaning in documents: " + minNumWords2);
        System.out.println(
                "Max Number of words after cleaning in documents: " + maxNumWords2);
        System.out.println();

        System.out.println(
                "Number of unique words after stemming: " + stemmedWords.size());
        System.out.println(
                "Average unique words after stemming: " + stemmedWords.size() / arrayHTMLfile.length);
        System.out.println(
                "Top 5 frequent words after stemming: ");
        fw = nfrequentStemmedWords.removeFirst();

        System.out.println(
                "   " + wordDictionary.get(fw.getID()) + " with " + fw.getCount() + " apparitions in documents.");
        fw = nfrequentStemmedWords.removeFirst();

        System.out.println(
                "   " + wordDictionary.get(fw.getID()) + " with " + fw.getCount() + " apparitions in documents.");
        fw = nfrequentStemmedWords.removeFirst();

        System.out.println(
                "   " + wordDictionary.get(fw.getID()) + " with " + fw.getCount() + " apparitions in documents.");
        fw = nfrequentStemmedWords.removeFirst();

        System.out.println(
                "   " + wordDictionary.get(fw.getID()) + " with " + fw.getCount() + " apparitions in documents.");
        fw = nfrequentStemmedWords.removeFirst();

        System.out.println(
                "   " + wordDictionary.get(fw.getID()) + " with " + fw.getCount() + " apparitions in documents.");
        System.out.println();
        // Fin Estadísticas

        /*
         //Playground
         String palabra = "camera";
         Integer idPalabra = idWordDictionary.get(palabra);
         Collection<FileFrequency> aparicionesPalabra = wordFrequency.get(idPalabra).values();
         Iterator<FileFrequency> it = aparicionesPalabra.iterator();
        
         while(it.hasNext()){
         FileFrequency ff = it.next();
         Integer idFile = ff.getID();
         String documento = fileDictionary.get(idFile);
         System.out.println("La palabra " + palabra + " aparece en el documento " + documento + " " + ff.getCount() + " veces.");
         }
         //End Playground
         */
    }

}
