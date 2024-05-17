package utb.fai.Keyword.Assert;

import java.util.concurrent.CopyOnWriteArrayList;

import utb.fai.Core.Keyword;
import utb.fai.Core.MessageBuffer.NATTMessage;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat trzeni ze definovana oblast v bufferu prijatych zprav
 * obsahem shoduje u dvou specifikovanych modulu ktery tyto zpravy
 * prijali/vygenerovali
 */
@NATTAnnotation.Keyword(name = "assert_range")
public class AssertRangeKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String module1Name;
    protected String module2Name;
    protected Long start;
    protected Long count;
    protected String rule;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private String notEqualStr;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        this.notEqualStr = null;

        if (start < 0) {
            throw new InternalErrorException("Start index cannot be negative number");
        }
        if (count <= 0) {
            throw new InternalErrorException("Count must be a number greater than 0");
        }

        // zpracovani promennych v retezci
        this.module1Name = VariableProcessor.processVariables(this.module1Name);
        this.module2Name = VariableProcessor.processVariables(this.module2Name);
        this.rule = VariableProcessor.processVariables(this.rule);

        if (result == null) {
            this.result = true;
        }
        if (this.rule == null) {
            this.rule = "";
        }
        String[] ruleParts = null;
        String separator = null;
        if (!this.rule.isEmpty()) {
            String[] parts = rule.split("\\|");
            if (parts.length < 2) {
                throw new InternalErrorException(
                        "Invalid format of rule. Correct format is: <message separator>|<X>;<X>;... Where is <X> is # (equals), ? (arbitrary), number 0-100 diff tolerance.");
            }
            separator = parts[0];
            ruleParts = parts[1].split(";");
        }

        // ziskani hodnot
        CopyOnWriteArrayList<NATTMessage> messages1 = NATTContext.instance().getMessageBuffer()
                .getMessages(module1Name);
        CopyOnWriteArrayList<NATTMessage> messages2 = NATTContext.instance().getMessageBuffer()
                .getMessages(module2Name);

        // oba buffery jsou prazde => shoda => pokud je ocekavano true navrati true,
        // pokud false tak false
        if (messages1.isEmpty() && messages2.isEmpty()) {
            return this.result;
        }

        // porovnani vsech hodnot
        int end = start.intValue() + count.intValue();
        String msg1, msg2;
        for (int i = start.intValue(); i < messages1.size() && i < messages2.size() && i < end; ++i) {
            // ziskani obsahu zprav
            msg1 = messages1.get(i).getMessage();
            msg2 = messages2.get(i).getMessage();

            // zde se porovna obsah zprav
            if (rule.isEmpty()) {
                // bez porovnavaciho pravidla
                if (msg1.equals(msg2) != this.result) {
                    if (this.result) {
                        notEqualStr = String.format("At index %d value '%s' is not equal to '%s'", i, msg1,
                                msg2);
                    } else {
                        notEqualStr = String.format("At index %d value '%s' is equal to '%s'", i, msg1,
                                msg2);
                    }
                    logger.warning(String.format("Assertion failed. %s", notEqualStr));
                    return false;
                }
            } else if (ruleParts != null) {
                // s porovnavacim pravidlem
                // zde dokonci implementaci ... "X;X;..." X nahrad symbolem: # - equals, ? -
                // libovolna hodnota, 99 (cislo) - tolerance %
                boolean match = true;
                String[] msg1Parts = msg1.split(separator);
                String[] msg2Parts = msg2.split(separator);
                if (msg1Parts.length != msg2Parts.length) {
                    match = false;
                } else {
                    for (int j = 0; j < ruleParts.length; j++) {
                        if (j >= msg1Parts.length) {
                            break;
                        }
                        // porovnavani casti zpravy
                        switch (ruleParts[j]) {
                            case "#":
                                // equals
                                if (!msg1Parts[j].equals(msg2Parts[j])) {
                                    match = false;
                                    break;
                                }
                                break;
                            case "?":
                                // libovolna hodnota
                                continue;
                            default:
                                // tolerance v % (jen pro cisla)
                                try {
                                    int tolerance = Integer.parseInt(ruleParts[j]);
                                    double val1 = Double.parseDouble(msg1Parts[j]);
                                    double val2 = Double.parseDouble(msg2Parts[j]);
                                    if (Math.abs((val1 - val2) / Math.min(val1, val2)) > (tolerance / 100.0)) {
                                        match = false;
                                        break;
                                    }
                                } catch (NumberFormatException ex) {
                                    logger.warning("Invalid number format in message or in rule!");
                                    return false;
                                }
                                break;
                        }
                    }
                }
                if (match != this.result) {
                    // neodpovida vysledku
                    if (this.result) {
                        notEqualStr = String.format(
                                "At index %d value '%s' is not equal to '%s'. (Comparison rule: %s)'", i, msg1,
                                msg2, this.rule);
                    } else {
                        notEqualStr = String.format("At index %d value '%s' is equal to '%s' (Comparison rule: %s)", i,
                                msg1,
                                msg2, this.rule);
                    }
                    logger.warning(String.format("Assertion failed. %s", notEqualStr));
                    return false;
                }
            } else {
                // neplatne pravidlo porovnavani
                return false;
            }
        }

        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // module1_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module1_name", Keyword.ParameterValueType.STRING,
                true);
        module1Name = (String) val.getValue();

        // module2_name (string) [je vyzadovany]
        val = this.getParameterValue("module2_name", Keyword.ParameterValueType.STRING,
                true);
        module2Name = (String) val.getValue();

        // start (long) [je vyzadovany]
        val = this.getParameterValue("start", Keyword.ParameterValueType.LONG,
                true);
        start = (Long) val.getValue();

        // count (long) [je vyzadovany]
        val = this.getParameterValue("count", Keyword.ParameterValueType.LONG,
                true);
        count = (Long) val.getValue();

        // rule (string) [je vyzadovany]
        val = this.getParameterValue("rule", Keyword.ParameterValueType.STRING,
                false);
        rule = (String) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", Keyword.ParameterValueType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        if (notEqualStr == null) {
            String message = String.format("<font color=\"green\">Assertion succeeded.</font>");
            return super.getDescription() + "<br>" + message;
        } else {
            String message = String.format("<font color=\"red\">Assertion failed. %s</font>", notEqualStr);
            return super.getDescription() + "<br>" + message;
        }
    }

}
