package utb.fai.natt.keyword.General;

import java.io.IOException;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.io.NetworkIO;

/**
 * Umoznuje ulozit obsah do souboru umisteneho na siti
 */
@NATTAnnotation.Keyword(
    name = "write_net_file",
    description = "Writes the defined content into a file on the network device.",
    parameters = {"file_url", "content"},
    types = {ParamValType.STRING, ParamValType.STRING}
    )
public class WriteNetFileKw extends NATTKeyword {

    protected String fileUrl;
    protected String content;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
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
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // file_url (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("file_url", NATTKeyword.ParamValType.STRING,
                true);
        fileUrl = (String) val.getValue();

        // content (string) [je vyzadovany]
        val = this.getParameterValue("content", NATTKeyword.ParamValType.STRING,
                true);
        content = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
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
