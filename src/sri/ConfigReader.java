package sri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Fasgort
 */
public class ConfigReader {

    private static ConfigReader instance = null;
    private boolean read = false;
    private String debug = "false";
    private String serialize = "false";
    private String dirResources = null;
    private String stopWordFilename = null;
    private String stringDirColEn = null;
    private String stringDirColEnN = null;
    private String stringDirColEnStop = null;
    private String stringDirColEnStem = null;
    private String stringDirColEnSer = null;
    private String stringDirDictionary = null;

    private ConfigReader(String stringConfData) {

        File confData = new File(stringConfData);

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
                        case "serialize":
                            serialize = valor;
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
                        case "stringDirColEnSer":
                            stringDirColEnSer = valor;
                            break;
                        case "stringDirDictionary":
                            stringDirDictionary = valor;
                            break;
                    }
                }
            }
            br.close();
            read = true;
        } catch (Exception e) {
            System.out.println("Config file couldn't load. It must be included with the executable.");
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
        if (dirResources == null || stopWordFilename == null
                || stringDirColEn == null || stringDirColEnN == null
                || stringDirColEnStop == null || stringDirColEnStem == null) {
            System.out.println("Config file syntax is wrong. SRI can't load correctly.");
            return true;
        }
        return false;
    }

    public String getDebug() {
        return debug;
    }

    public String getSerialize() {
        return serialize;
    }

    public String getDirResources() {
        return dirResources;
    }

    public String getStopWordFilename() {
        return stopWordFilename;
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

    public String getStringDirColEnSer() {
        return stringDirColEnSer;
    }

    public String getStringDirDictionary() {
        return stringDirDictionary;
    }

}
