package utb.fai.natt.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.yaml.snakeyaml.Yaml;

import utb.fai.natt.spi.NATTLogger;

/**
 * Trida pro IO operace ze soubory na localhostu
 */
public class LocalHostIO {

    private NATTLogger logger = new NATTLogger(LocalHostIO.class);

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalHostIO(String fileName) {
        this.fileName = fileName;
    }

    public void saveToYaml(Object data) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        Yaml yaml = new Yaml();
        yaml.dump(data, writer);
        writer.close();
        logger.info(String.format("Object data has been saved to a file in yaml format (%s)", this.fileName));
    }

    public Object loadFromYaml() throws IOException {
        FileReader reader = new FileReader(fileName);
        Yaml yaml = new Yaml();
        Object data = yaml.load(reader);
        reader.close();  
        logger.info(String.format("Yaml data has been loaded from file (%s)", this.fileName));
        return data;
    }

    public void saveText(String text) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(text);
        writer.close();
        logger.info(String.format("Text has been saved to a file (%s)", this.fileName));
    }

    public String loadText() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        logger.info(String.format("Text has been loaded from file (%s)", this.fileName));
        return sb.toString();
    }

}
