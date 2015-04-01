package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fasgort
 */
public class ConfigReader {

    private static ConfigReader instance = null;
    private boolean read = false;
    private boolean debug = false;
    private boolean serialize = false;
    private int documentsRecovered = 5;
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
                            debug = Boolean.parseBoolean(valor);
                            break;
                        case "serialize":
                            serialize = Boolean.parseBoolean(valor);
                            break;
                        case "documentsRecovered":
                            documentsRecovered = Integer.parseInt(valor);
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

    public boolean fail() {
        if (read == false) {
            return true;
        }
        if (dirResources == null
                || stringDirColEn == null
                || stringDirColEnN == null
                || stringDirColEnStop == null
                || stringDirColEnStem == null
                || stringDirIndex == null
                || stopWordFilename == null
                || stringFileDictionary == null
                || stringWordDictionary == null
                || stringFrequencyIndex == null
                || stringWeightIndex == null
                || stringSearchFile == null) {
            System.out.println("Config file syntax is wrong. SRI can't load correctly.");
            return true;
        }
        return false;
    }

    public boolean getDebug() {
        return debug;
    }

    public boolean getSerialize() {
        return serialize;
    }

    public int getDocumentsRecovered() {
        return documentsRecovered;
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
