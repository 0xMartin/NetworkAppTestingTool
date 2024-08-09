package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.WebCrawler.Parser;
import utb.fai.Module.WebCrawler.WebCrawler;
import utb.fai.Module.WebCrawler.WordFrequencyAnalyzer;

/**
 * Umoznuje definovat modul pro webcrawler
 */
@NATTAnnotation.Keyword(name = "create_web_crawler")
public class CreateWebCrawlerKw extends Keyword {

    protected String moduleName;
    protected String startURL;
    protected Long maxDepth;
    protected String analyzer;

    private WebCrawler module;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.startURL = VariableProcessor.processVariables(this.startURL);
        this.analyzer = VariableProcessor.processVariables(this.analyzer);

        // volba analyzatoru
        Parser.Analyzer analyzerObj = null;
        // analyzator absolutni cetnosti slov. Format: "word-freq:X" kde X predstavuje
        // pocet slov
        if (analyzer.startsWith("word-freq:")) {
            String[] parts = analyzer.split(":");
            if (parts.length <= 1) {
                throw new InternalErrorException("Invalid analyzer option");
            }
            try {
                analyzerObj = new WordFrequencyAnalyzer(Math.abs(Integer.parseInt(parts[1])));
            } catch (NumberFormatException ex) {
                throw new InternalErrorException("Invalid word frequency analyzer word cound");
            }
        }

        // vytovreni modulu
        int threadCount = Runtime.getRuntime().availableProcessors();
        this.module = new WebCrawler(this.moduleName, this.startURL, this.maxDepth.intValue(), threadCount, analyzerObj);
        this.module.runModule();

        return this.module.isRunning();
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        if(this.module != null) {
            this.module.terminateModule();
        }
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // start_url (string) [je vyzadovany]
        val = this.getParameterValue("start_url", Keyword.ParameterValueType.STRING,
                true);
        startURL = (String) val.getValue();

        // max_depth (long) [je vyzadovany]
        val = this.getParameterValue("max_depth", Keyword.ParameterValueType.LONG,
                true);
        maxDepth = (Long) val.getValue();

        // analyzer (string) [je vyzadovany]
        val = this.getParameterValue("analyzer", Keyword.ParameterValueType.STRING,
                true);
        analyzer = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public String getDescription() {
        String message;
        if (this.module == null) {
            message = String.format("<font color=\"red\">Failed to start module with name '%s'.</font>",
                    this.moduleName);
        }
        if (this.module.isRunning()) {
            message = String.format("<font color=\"green\">The module with name '%s' is running.</font>",
                    this.moduleName);
        } else {
            message = String.format("<font color=\"red\">Failed to start module with name '%s'.</font>",
                    this.moduleName);
        }
        return super.getDescription() + "<br>" + message;
    }
    
}
