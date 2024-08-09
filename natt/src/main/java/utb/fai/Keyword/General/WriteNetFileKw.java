package utb.fai.Keyword.General;

import java.io.IOException;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.IO.NetworkIO;

/**
 * Umoznuje ulozit obsah do souboru umisteneho na siti
 */
@NATTAnnotation.Keyword(name = "write_net_file")
public class WriteNetFileKw extends Keyword {

    protected String fileUrl;
    protected String content;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.fileUrl = VariableProcessor.processVariables(this.fileUrl);
        this.content = VariableProcessor.processVariables(this.content);

        NetworkIO networkIO = new NetworkIO(fileUrl);
        try {
            networkIO.saveText(this.content);
        } catch (IOException e) {
            return false;
        }

        status = true;
        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // file_url (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("file_url", Keyword.ParameterValueType.STRING,
                true);
        fileUrl = (String) val.getValue();

        // content (string) [je vyzadovany]
        val = this.getParameterValue("content", Keyword.ParameterValueType.STRING,
                true);
        content = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String message;
        if (!status) {
            message = "<br><font color=\"red\">Failed to write content to file.</font>";
        } else {
            String data = this.content.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            message = String.format(
                    "<font color=\"green\">The following content has been written to file <b>[%s]</b>: <b>'%s'</b></font>",
                    this.fileUrl, data);
        }
        return super.getDescription() + "<br>" + message;
    }

}
