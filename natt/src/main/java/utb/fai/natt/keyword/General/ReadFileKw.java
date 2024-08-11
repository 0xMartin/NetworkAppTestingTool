package utb.fai.natt.keyword.General;

import java.io.IOException;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.io.LocalHostIO;

/**
 * Umoznuje precist obsah textoveho souboru a jeho obsah vlozit do promenne
 */
@NATTAnnotation.Keyword(name = "read_file")
public class ReadFileKw extends NATTKeyword {

    protected String varName;
    protected String filePath;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.filePath = VariableProcessor.processVariables(this.filePath);

        LocalHostIO hostIO = new LocalHostIO(filePath);
        String fileText;
        try {
            fileText = hostIO.loadText();
        } catch (IOException e) {
            return false;
        }

        this.varName = NATTContext.instance().storeValueToVariable(varName, fileText);
        if (this.varName != null) {
            status = true;
        }

        return this.varName != null;
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", NATTKeyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // file_path (string) [je vyzadovany]
        val = this.getParameterValue("file_path", NATTKeyword.ParameterValueType.STRING,
                true);
        filePath = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.varName != null) {
            NATTContext.instance().getVariables().remove(this.varName);
        }
    }

    @Override
    public String getDescription() {
        if (this.varName == null || status == false) {
            return super.getDescription() + "<br><font color=\"red\">Failed to store value to variable.</font>";
        }
        String data = NATTContext.instance().getVariable(this.varName);
        data = data.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        return super.getDescription() + String.format(
                "<br><font color=\"green\">The following content of file has been stored in a variable named <b>[%s]</b>: <b>'%s'</b></font>",
                this.varName, data);
    }

}