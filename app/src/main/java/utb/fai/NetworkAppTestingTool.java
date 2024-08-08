package utb.fai;

import java.io.IOException;

import utb.fai.Core.NATTCore;
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
        NATTCore core = null;
        try {
            // inicializace nastroje
            core = new NATTCore(args);

            // nacte soubor s konfiguraci testu
            core.loadConfigFile();

            // sestavi strukturu testovani podle nactene konfigurace
            core.buildTestsFromYaml();

            // vykonani vsech testu
            core.executeAllTests();

            // vygenerovani reportu (v pripade uspeni vsech testovacich pripadu)
            core.generateReport();

        } catch (InternalErrorException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            System.exit(e.getErrorCode());

        } catch (InvalidSyntaxInConfigurationException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            System.exit(e.getErrorCode());

        } catch (IOException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            System.exit(StatusCode.INTERNAL_ERROR);

        } catch (NonUniqueModuleNamesException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            System.exit(e.getErrorCode());

        } catch (NonUniqueTestNamesException e) {
            e.printStackTrace();
            NATTCore.termiteAllModules();
            System.exit(e.getErrorCode());

        }
    }

}
