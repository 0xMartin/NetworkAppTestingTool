package utb.fai;

import java.io.IOException;

import utb.fai.Core.NATTCore;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.StatusCode;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.NonUniqueTestNamesException;

/**
 * Hlavni metoda univerzalniho black box testovaciho a hodnoticiho nastroje pro
 * sitove aplikace, ktery vznikl jako soucast diplomove prace jako nastroj pro
 * automatizovane hodnoceni.
 */
public class NetworkAppTestingTool {

    public static void main(String[] args) {
        try {
            // inicializace nastroje
            NATTCore core = new NATTCore(args);

            // nacte soubor s konfiguraci testu
            core.loadConfigFile();

            // sestavi strukturu testovani podle nactene konfigurace
            core.buildTestsFromYaml();

            // vykonani vsech testu
            core.executeAllTests();

            // vygenerovani reportu (v pripade uspeni vsech testovacich pripadu)
            core.generateReport();

            // ukonci zapis do log souboru
            NATTLogger.LogFileWriter.getInstance().close();

        } catch (InternalErrorException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            NATTLogger.LogFileWriter.getInstance().close();
            System.exit(e.getErrorCode());

        } catch (InvalidSyntaxInConfigurationException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            NATTLogger.LogFileWriter.getInstance().close();
            System.exit(e.getErrorCode());

        } catch (IOException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            NATTLogger.LogFileWriter.getInstance().close();
            System.exit(StatusCode.INTERNAL_ERROR);

        } catch (NonUniqueModuleNamesException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            NATTLogger.LogFileWriter.getInstance().close();
            System.exit(e.getErrorCode());

        } catch (NonUniqueTestNamesException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            NATTLogger.LogFileWriter.getInstance().close();
            System.exit(e.getErrorCode());

        }
    }

}
