package utb.fai.natt.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.yaml.snakeyaml.Yaml;

import utb.fai.natt.spi.NATTLogger;

/**
 * Trida pro IO operace ze soubory po siti
 */
public class NetworkIO {

    private NATTLogger logger = new NATTLogger(NetworkIO.class);

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public NetworkIO(String url) {
        this.url = url;
    }

    public void saveToYaml(Object data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-yaml");

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        Yaml yaml = new Yaml();
        yaml.dump(data, writer);
        writer.close();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to save YAML data. HTTP error code: " + responseCode);
        }
        logger.info(String.format("Object data has been saved to a network location in yaml format (%s)", this.url));
    }

    public Object loadFromYaml() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/x-yaml");

        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        Yaml yaml = new Yaml();
        Object data = yaml.load(reader);
        reader.close();
        logger.info(String.format("Yaml data has been loaded from network location (%s)", this.url));
        return data;
    }

    public void saveText(String text) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(text);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to save text data. HTTP error code: " + responseCode);
        }
        logger.info(String.format("Text has been saved to a network location (%s)", this.url));
    }

    public String loadText() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            logger.info(String.format("Text has been loaded from network location (%s)", this.url));
            return sb.toString();
        }
    }

}
