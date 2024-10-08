package utb.fai.natt.module.WebCrawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utb.fai.natt.spi.NATTLogger;

/**
 * Parer pro webove stranky. Hleda na strance nove odkazy a predava dokument
 * webove stranky univerzalnimu analyzatoru
 */
public class Parser implements Runnable {

	protected NATTLogger logger = new NATTLogger(Parser.class);

	public static String CSS_QUERY_SELECTOR = "a[href], frame[src], iframe[src]";

	private final WebCrawler crawler;
	private final WebCrawler.URLinfo urlInfo;
	private String pageCharSet;

	public Parser(WebCrawler crawler, WebCrawler.URLinfo urlInfo) {
		this.crawler = crawler;
		this.urlInfo = urlInfo;
		this.crawler.getVisitedURIs().add(urlInfo.uri.toString());
	}

	@Override
	public void run() {
		if (urlInfo.uri.toString().isEmpty())
			return;

		try {
			// kodovani stranky
			this.pageCharSet = Parser.getCharSet(urlInfo.uri);
			// stazeni stranky
			/*
			 * Document doc = Jsoup.parse(new URL(urlInfo.uri.toString()).openStream(),
			 * "UTF-8", urlInfo.uri.toString());
			 */
			Document doc = Jsoup.connect(validURL(urlInfo.uri.toString())).get();

			// analyza + hledani linku
			try {
				this.crawler.getAnalyzer().analyze(doc, this.pageCharSet);
				if (urlInfo.depth + 1 <= this.crawler.getMaxDetph()) {
					findLinks(doc, urlInfo.depth + 1);
				}
			} catch (Exception e) {
				logger.warning(String.format("Failed to parse web page '%s'", urlInfo.uri.toString()));
			}

		} catch (IOException e1) {
		}
	}

	/**
	 * Na webove strance nalezne dalsi odkazi
	 * 
	 * @param doc   JSOUP Document
	 * @param depth Aktualni hloubka analyzi
	 */
	public void findLinks(Document doc, int depth) {
		Elements newsHeadlines = doc.select(CSS_QUERY_SELECTOR);
		for (Element headline : newsHeadlines) {
			try {
				this.crawler.getURLQueue().add(new WebCrawler.URLinfo(headline.absUrl("href"), depth));
			} catch (URISyntaxException e) {
			}
		}
	}

	private static String getCharSet(URI uri) {
		String charset = "UTF-8";
		try {
			URLConnection conn = uri.toURL().openConnection();
			String type = conn.getContentType();
			int encodingIndex = type.indexOf(';');
			if (encodingIndex >= 0) {
				String encoding = type.substring(type.indexOf(';') + 2);
				charset = encoding.substring(encoding.indexOf('=') + 1);
			}
		} catch (Exception e) {
		}
		return charset;
	}

	private static String validURL(String urlStr) {
		try {
			URL url = new URL(URLDecoder.decode(urlStr, StandardCharsets.UTF_8.toString()));
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
					url.getQuery(), url.getRef());
			return uri.toString();
		} catch (URISyntaxException | UnsupportedEncodingException | MalformedURLException e) {
			return null;
		}
	}

	/**************************************************************************************************/
	// LOCAL CLASSES
	/**************************************************************************************************/

	/**
	 * Rozhrani analayzatoru
	 * 
	 * @author Martin Krcma
	 */
	public static interface Analyzer {
		public void analyze(Document doc, String charSet) throws Exception;

		public void printResult(String moduleName);
	}

}
