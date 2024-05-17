package utb.fai.Module.WebCrawler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.NATTModule;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Tento modul obashuje implementaci multi-vlaknoveho webcrawleru. Ve vice
 * vlaknech prochazi webove stranky a analyzuje je. Vystup je zavysli na
 * pouzitem analyzatoru.
 */
@NATTAnnotation.Module("web-crawler")
public class WebCrawler extends NATTModule {

	protected NATTLogger logger = new NATTLogger(WebCrawler.class);

	private final Parser.Analyzer analyzer;
	private final ThreadPoolExecutor executor;
	private final List<URLinfo> urlQueue;
	private final Set<String> visitedURIs;

	protected int maxDepth;
	protected String startUrl;

	/**
	 * Vytvori instacni webcrawleru
	 * 
	 * @param name        Jmeno modulu
	 * @param startUrl    Startovni URL adresa
	 * @param maxDepth    Maximalni hloubka analyzy webu
	 * @param threadCount Pocet vlaken
	 * @param analyzer    Analyzator weboveho obsahu
	 * @throws NonUniqueModuleNamesException
	 */
	public WebCrawler(String name, String startUrl, int maxDepth, int threadCount, Parser.Analyzer analyzer)
			throws NonUniqueModuleNamesException, InternalErrorException {
		super(name);
		this.startUrl = startUrl;
		this.maxDepth = maxDepth;
		this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(1, threadCount));
		this.urlQueue = Collections.synchronizedList(new LinkedList<URLinfo>());
		this.visitedURIs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.analyzer = analyzer;
	}

	@Override
	public void runModule() throws InternalErrorException {
		logger.info(super.getNameForLogger() + "Webcrawler is running now. Start URL: " + this.startUrl);
		try {
			this.startWebCrawler(this.startUrl);
		} catch (URISyntaxException e) {
			logger.warning(super.getNameForLogger() + "Webcraler failed. Message: " + e.getMessage());
			return;
		}
		this.printResult();
		this.setRunning(true);
	}

	@Override
	public boolean terminateModule() {
		// odstraneni tohoto modulu z aktivnich modulu
		NATTContext.instance().getModules().remove(this);
		return true;
	}

	@Override
	public boolean sendMessage(String message) throws InternalErrorException {
		return false;
	}

	public List<URLinfo> getURLQueue() {
		return this.urlQueue;
	}

	public Set<String> getVisitedURIs() {
		return this.visitedURIs;
	}

	public int getMaxDetph() {
		return this.maxDepth;
	}

	public Parser.Analyzer getAnalyzer() {
		return this.analyzer;
	}

	/**
	 * Analyzuje webovou stranku
	 * 
	 * @param url       - URL adresa webove stranky
	 * @param max_depth - maximalni hloubka analyzi
	 * @throws URISyntaxException
	 */
	public void startWebCrawler(String url) throws URISyntaxException {
		long start = System.currentTimeMillis();

		urlQueue.add(new URLinfo(url, 0));

		URLinfo urlInfo;
		while (!urlQueue.isEmpty() || this.executor.getActiveCount() > 0) {
			if (!urlQueue.isEmpty()) {
				urlInfo = urlQueue.get(0);
				// zavola parser pro danou URL pokud jeste nebyla navstivena
				if (!this.visitedURIs.contains(urlInfo.uri.toString()) && !urlInfo.uri.isOpaque()) {
					this.executor.execute(new Parser(this, urlInfo));
				}
				urlQueue.remove(0);
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		logger.info(super.getNameForLogger() + String.format("Job finished [elapsed time: %.3f s]",
				(System.currentTimeMillis() - start) / 1e3));
	}

	/**
	 * Vypise vysledek analyzy
	 */
	public void printResult() {
		this.analyzer.printResult(this.getName());
	}

	/**************************************************************************************************/
	// LOCAL CLASSES
	/**************************************************************************************************/

	public static class URLinfo {
		URI uri;
		int depth;

		public URLinfo(String url, int depth) throws URISyntaxException {
			this.uri = new URI(url);
			this.depth = depth;
		}
	}

}
