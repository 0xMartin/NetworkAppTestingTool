package utb.fai.natt.keyword.Assert;

import java.util.concurrent.CopyOnWriteArrayList;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.INATTMessage;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje definovat trzeni ze definovana oblast v bufferu prijatych zprav
 * obsahem shoduje u dvou specifikovanych modulu ktery tyto zpravy
 * prijali/vygenerovali
 */
@NATTAnnotation.Keyword(
    name = "assert_range",
    description = "Verifies if the sequence of received messages from two modules falls within a specified segment. Simple comparison rules can also be defined for comparison.",
    parameters = {"module1_name", "module2_name", "start", "count", "rule", "result"},
    types = {ParamValType.STRING, ParamValType.STRING, ParamValType.LONG, ParamValType.LONG, ParamValType.STRING, ParamValType.BOOLEAN},
    kwGroup = "NATT Assert"
    )
public class AssertRangeKw extends NATTKeyword {

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
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
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
        CopyOnWriteArrayList<INATTMessage> messages1 = NATTContext.instance().getMessageBuffer()
                .getMessages(module1Name);
        CopyOnWriteArrayList<INATTMessage> messages2 = NATTContext.instance().getMessageBuffer()
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
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // module1_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module1_name", NATTKeyword.ParamValType.STRING,
                true);
        module1Name = (String) val.getValue();

        // module2_name (string) [je vyzadovany]
        val = this.getParameterValue("module2_name", NATTKeyword.ParamValType.STRING,
                true);
        module2Name = (String) val.getValue();

        // start (long) [je vyzadovany]
        val = this.getParameterValue("start", NATTKeyword.ParamValType.LONG,
                true);
        start = (Long) val.getValue();

        // count (long) [je vyzadovany]
        val = this.getParameterValue("count", NATTKeyword.ParamValType.LONG,
                true);
        count = (Long) val.getValue();

        // rule (string) [je vyzadovany]
        val = this.getParameterValue("rule", NATTKeyword.ParamValType.STRING,
                false);
        rule = (String) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", NATTKeyword.ParamValType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
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
