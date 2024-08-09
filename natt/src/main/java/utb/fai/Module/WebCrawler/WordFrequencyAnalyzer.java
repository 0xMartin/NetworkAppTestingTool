package utb.fai.Module.WebCrawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import utb.fai.Core.NATTContext;

/**
 * Trida pro analyzu četnosti slov na webovych strankach
 * 
 * format vypisu (pocet slov zavysi na definovanem cisle):
 * tag: "word-1" message: <slovo>;<absolutni cetnost>
 * tag: "word-2" message: <slovo>;<absolutni cetnost>
 * ...
 */
public class WordFrequencyAnalyzer implements Parser.Analyzer {

	private final Map<String, Word> wordCounter;
	private final int worldCount;

	public WordFrequencyAnalyzer(int worldCount) {
		this.worldCount = worldCount;
		this.wordCounter = Collections.synchronizedMap(new HashMap<String, Word>());
	}

	@Override
	public void analyze(Document doc, String charSet) throws Exception {
		String text = doc.body().text();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(text.getBytes()), charSet));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] words = line.split("\\s+");
			for (String word : words) {
				Word w = this.wordCounter.get(word);
				if (w == null) {
					w = new Word(word);
					this.wordCounter.put(word, w);
				}
				w.count++;
			}
		}
		reader.close();
	}

	@Override
	public void printResult(String moduleName) {
		List<Word> sortedWords = new ArrayList<Word>(this.wordCounter.values());
		Collections.sort(sortedWords);
		// vypis vsech slov ve formatu <slovo>;<absolutni cetnost>
		int i = 0;
		for (Word word : sortedWords) {
			i++;
			if (i > this.worldCount)
				break;
			NATTContext.instance().getMessageBuffer().addMessage(moduleName, "word-" + i, word.word + ";" + word.count);
		}
	}

}

/**
 * Slovo, se ktery pracuje tento analyzator
 * 
 * @author Martin Krcma
 */
class Word implements Comparable<Word> {
	String word;
	int count;

	public Word(String word) {
		this.word = word;
	}

	@Override
	public int compareTo(Word b) {
		return b.count - count;
	}
}
