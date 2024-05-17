package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Before;
import org.junit.Test;

import utb.fai.Core.MessageBuffer.NATTMessage;
import utb.fai.Core.NATTContext;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.WebCrawler.WebCrawler;
import utb.fai.Module.WebCrawler.WordFrequencyAnalyzer;

public class WebCrawlerTest {

    private WebCrawler webCrawler;

    @Before
    public void setUp() throws InternalErrorException, NonUniqueModuleNamesException {
        Runtime runtime = Runtime.getRuntime();
        int numOfProcessors = runtime.availableProcessors();
        webCrawler = new WebCrawler("TestWebCrawler",
                "https://google.com",
                1,
                numOfProcessors,
                new WordFrequencyAnalyzer(20));
    }

    @Test
    public void testStartWebCrawler() throws InternalErrorException {
        webCrawler.runModule();
        assertTrue(webCrawler.getVisitedURIs().contains("https://google.com"));

        CopyOnWriteArrayList<NATTMessage> messages = NATTContext.instance().getMessageBuffer()
                .getMessages("TestWebCrawler");
        assertEquals(20, messages.size());
    }

}