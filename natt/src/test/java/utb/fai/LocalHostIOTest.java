package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import utb.fai.IO.LocalHostIO;

public class LocalHostIOTest {

    private final String yamlFileName = "test.yaml";
    private final String textFileName = "test.txt";

    @Test
    public void saveAndLoadYamlTest() throws IOException {
        LocalHostIO io = new LocalHostIO(yamlFileName);

        Map<String, Object> dataToSave = Map.of("name", "John Doe", "age", 30);

        io.saveToYaml(dataToSave);

        Object loadedData = io.loadFromYaml();

        assertTrue(loadedData instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> loadedMap = (Map<String, Object>) loadedData;
        assertEquals("John Doe", loadedMap.get("name"));
        assertEquals(30, loadedMap.get("age"));

        deleteFile(yamlFileName);
    }

    @Test
    public void saveAndLoadTextTest() throws IOException {
        LocalHostIO io = new LocalHostIO(textFileName);

        String textToSave = "Hello, world!";

        io.saveText(textToSave);

        String loadedText = io.loadText();

        assertEquals(textToSave + "\n", loadedText);

        deleteFile(textFileName);
    }

    private void deleteFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
