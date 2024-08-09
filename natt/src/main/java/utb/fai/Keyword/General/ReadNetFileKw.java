package utb.fai.Keyword.General;

import java.io.IOException;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.IO.NetworkIO;

/**
 * Umoznuje precist obsah textoveho souboru ze site a jeho obsah vlozit do promenne
 */
@NATTAnnotation.Keyword(name = "read_net_file")
public class ReadNetFileKw extends Keyword {

    protected String varName;
    protected String fileUrl;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.fileUrl = VariableProcessor.processVariables(this.fileUrl);

        NetworkIO networkIO = new NetworkIO(fileUrl);
        String fileText;
        try {
            fileText = networkIO.loadText();
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", Keyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // file_url (string) [je vyzadovany]
        val = this.getParameterValue("file_url", Keyword.ParameterValueType.STRING,
                true);
        fileUrl = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
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
