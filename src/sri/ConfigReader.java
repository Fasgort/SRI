package sri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fasgort
 */
public class ConfigReader {

    private static volatile ConfigReader instance = null;
    private boolean read = false;
    private String debug = null;
    private String serialize = null;
    private String documentsRecovered = null;
    private String filterInclude = null;
    private String filterExclude = null;
    private String filterFromPage = null;
    private String dirResources = null;
    private String stringDirColEn = null;
    private String stringDirColEnN = null;
    private String stringDirColEnStop = null;
    private String stringDirColEnStem = null;
    private String stringDirIndex = null;
    private String stopWordFilename = null;
    private String stringFileDictionary = null;
    private String stringWordDictionary = null;
    private String stringFrequencyIndex = null;
    private String stringWeightIndex = null;
    private String stringSearchFile = null;

    private ConfigReader(String stringConfData) {

        File confData = new File(stringConfData);

        try (FileReader fr = new FileReader(confData);
                BufferedReader br = new BufferedReader(fr)) {
            Pattern comment = Pattern.compile("^([\\w/.]+) = ((?:\".*\")|(?:[\\w/.]+))");
            Matcher m;
            String linea;
            while ((linea = br.readLine()) != null) {
                m = comment.matcher(linea);
                if (m.find()) {
                    String atributo;
                    String valor;

                    atributo = m.group(1).trim();
                    valor = m.group(2).trim();

                    if (valor.startsWith("\"") && valor.endsWith("\"")) {
                        valor = valor.substring(1, valor.length() - 1);
                    }

                    switch (atributo) {
                        case "debug":
                            debug = valor;
                            break;
                        case "serialize":
                            serialize = valor;
                            break;
                        case "documentsRecovered":
                            documentsRecovered = valor;
                            break;
                        case "filterInclude":
                            filterInclude = valor;
                            break;
                        case "filterExclude":
                            filterExclude = valor;
                            break;
                        case "filterFromPage":
                            filterFromPage = valor;
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
                        case "stringDirIndex":
                            stringDirIndex = valor;
                            break;
                        case "stringFileDictionary":
                            stringFileDictionary = valor;
                            break;
                        case "stringWordDictionary":
                            stringWordDictionary = valor;
                            break;
                        case "stringFrequencyIndex":
                            stringFrequencyIndex = valor;
                            break;
                        case "stringWeightIndex":
                            stringWeightIndex = valor;
                            break;
                        case "stringSearchFile":
                            stringSearchFile = valor;
                            break;
                    }
                }
            }
            br.close();
            read = true;
        } catch (IOException ex) {
            System.err.println(ex);
        }

    }

    public static ConfigReader getInstance(String stringConfData) {
        if (instance == null) {
            instance = new ConfigReader(stringConfData);
        }
        return instance;
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public boolean fail(String stringConfData) {
        if (read == false) {
            return true;
        } else {
            File confData = new File(stringConfData);
            try (FileWriter fw = new FileWriter(confData, true);
                    BufferedWriter bw = new BufferedWriter(fw)) {

                if (debug == null || (debug.compareToIgnoreCase("true") != 0 && debug.compareToIgnoreCase("false") != 0)) {
                    debug = "false";
                    bw.append("debug = " + debug);
                    bw.newLine();
                }

                if (serialize == null || (serialize.compareToIgnoreCase("true") != 0 && serialize.compareToIgnoreCase("false") != 0)) {
                    serialize = "true";
                    bw.append("serialize = " + serialize);
                    bw.newLine();
                }

                if (documentsRecovered == null) {
                    documentsRecovered = "5";
                    bw.append("documentsRecovered = " + documentsRecovered);
                    bw.newLine();
                } else {
                    try {
                        Integer.parseInt(documentsRecovered);
                    } catch (NumberFormatException ex) {
                        documentsRecovered = "5";
                        bw.append("documentsRecovered = " + documentsRecovered);
                        bw.newLine();
                    }
                }

                if (filterInclude == null) {
                    filterInclude = "";
                    bw.append("filterInclude = \"\"");
                    bw.newLine();
                }

                if (filterExclude == null) {
                    filterExclude = "";
                    bw.append("filterExclude = \"\"");
                    bw.newLine();
                }

                if (filterFromPage == null) {
                    filterFromPage = "";
                    bw.append("filterFromPage = \"\"");
                    bw.newLine();
                }

                if (dirResources == null) {
                    dirResources = "./resources/";
                    bw.append("dirResources = " + dirResources);
                    bw.newLine();
                }

                if (stringDirColEn == null) {
                    stringDirColEn = "./coleccionEn/";
                    bw.append("stringDirColEn = " + stringDirColEn);
                    bw.newLine();
                }

                if (stringDirColEnN == null) {
                    stringDirColEnN = "./coleccionEnNormalized/";
                    bw.append("stringDirColEnN = " + stringDirColEnN);
                    bw.newLine();
                }

                if (stringDirColEnStop == null) {
                    stringDirColEnStop = "./coleccionEnStopped/";
                    bw.append("stringDirColEnStop = " + stringDirColEnStop);
                    bw.newLine();
                }

                if (stringDirColEnStem == null) {
                    stringDirColEnStem = "./coleccionEnStemmed/";
                    bw.append("stringDirColEnStem = " + stringDirColEnStem);
                    bw.newLine();
                }

                if (stringDirIndex == null) {
                    stringDirIndex = "./index/";
                    bw.append("stringDirIndex = " + stringDirIndex);
                    bw.newLine();
                }

                if (stopWordFilename == null) {
                    stopWordFilename = "englishST.txt";
                    bw.append("stopWordFilename = " + stopWordFilename);
                    bw.newLine();
                }

                if (stringFileDictionary == null) {
                    stringFileDictionary = "fileDictionary.ser";
                    bw.append("stringFileDictionary = " + stringFileDictionary);
                    bw.newLine();
                }

                if (stringWordDictionary == null) {
                    stringWordDictionary = "wordDictionary.ser";
                    bw.append("stringWordDictionary = " + stringWordDictionary);
                    bw.newLine();
                }

                if (stringFrequencyIndex == null) {
                    stringFrequencyIndex = "frequencyIndex.ser";
                    bw.append("stringFrequencyIndex = " + stringFrequencyIndex);
                    bw.newLine();
                }

                if (stringWeightIndex == null) {
                    stringWeightIndex = "weightIndex.ser";
                    bw.append("stringWeightIndex = " + stringWeightIndex);
                    bw.newLine();
                }

                if (stringSearchFile == null) {
                    stringSearchFile = "consultas.txt";
                    bw.append("stringSearchFile = " + stringSearchFile);
                    bw.newLine();
                }
                return false;
            } catch (IOException ex) {
                System.err.println(ex);
                return true;
            }

        }

    }

    public boolean getDebug() {
        return Boolean.parseBoolean(debug);
    }

    public boolean getSerialize() {
        return Boolean.parseBoolean(serialize);
    }

    public int getDocumentsRecovered() {
        return Integer.parseInt(documentsRecovered);
    }

    public String getFilterInclude() {
        return filterInclude;
    }

    public String getFilterExclude() {
        return filterExclude;
    }

    public String getFilterFromPage() {
        return filterFromPage;
    }

    public String getDirResources() {
        return dirResources;
    }

    public String getStringDirColEn() {
        return stringDirColEn;
    }

    public String getStringDirColEnN() {
        return stringDirColEnN;
    }

    public String getStringDirColEnStop() {
        return stringDirColEnStop;
    }

    public String getStringDirColEnStem() {
        return stringDirColEnStem;
    }

    public String getStringDirIndex() {
        return stringDirIndex;
    }

    public String getStopWordFilename() {
        return stopWordFilename;
    }

    public String getStringFileDictionary() {
        return stringFileDictionary;
    }

    public String getStringWordDictionary() {
        return stringWordDictionary;
    }

    public String getStringFrequencyIndex() {
        return stringFrequencyIndex;
    }

    public String getStringWeightIndex() {
        return stringWeightIndex;
    }

    public String getStringSearchFile() {
        return stringSearchFile;
    }

}
