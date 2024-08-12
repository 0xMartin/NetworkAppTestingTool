package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.WebCrawler.Parser;
import utb.fai.natt.module.WebCrawler.WebCrawler;
import utb.fai.natt.module.WebCrawler.WordFrequencyAnalyzer;

/**
 * Umoznuje definovat modul pro webcrawler
 */
@NATTAnnotation.Keyword(
    name = "create_web_crawler",
    description = "Creates a module that launches a web crawler.",
    parameters = { "name", "start_url", "max_depth", "analyzer" },
    types = { ParamValType.STRING, ParamValType.STRING, ParamValType.LONG, ParamValType.STRING },
    kwGroup = "NATT Module"
    )
public class CreateWebCrawlerKw extends NATTKeyword {

    protected String moduleName;
    protected String startURL;
    protected Long maxDepth;
    protected String analyzer;

    private WebCrawler module;

    @Override
    public boolean execute(INATTContext ctx)
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
        this.module = new WebCrawler(this.moduleName, this.startURL, this.maxDepth.intValue(), threadCount,
                analyzerObj);
        this.module.runModule();

        return this.module.isRunning();
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.module != null) {
            this.module.terminateModule();
            NATTContext.instance().removeModule(this.moduleName);
        }
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();

        // start_url (string) [je vyzadovany]
        val = this.getParameterValue("start_url", NATTKeyword.ParamValType.STRING,
                true);
        startURL = (String) val.getValue();

        // max_depth (long) [je vyzadovany]
        val = this.getParameterValue("max_depth", NATTKeyword.ParamValType.LONG,
                true);
        maxDepth = (Long) val.getValue();

        // analyzer (string) [je vyzadovany]
        val = this.getParameterValue("analyzer", NATTKeyword.ParamValType.STRING,
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
