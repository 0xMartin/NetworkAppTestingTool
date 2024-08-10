package utb.fai.natt.keyword.General;

import java.io.IOException;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.io.LocalHostIO;

/**
 * Umoznuje ulozit obsah do souboru
 */
@NATTAnnotation.Keyword(name = "write_file")
public class WriteFileKw extends NATTKeyword {

    protected String filePath;
    protected String content;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.filePath = VariableProcessor.processVariables(this.filePath);
        this.content = VariableProcessor.processVariables(this.content);

        LocalHostIO hostIO = new LocalHostIO(filePath);
        try {
            hostIO.saveText(this.content);
        } catch (IOException e) {
            return false;
        }

        status = true;
        return true;
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // file_path (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("file_path", NATTKeyword.ParameterValueType.STRING,
                true);
        filePath = (String) val.getValue();

        // content (string) [je vyzadovany]
        val = this.getParameterValue("content", NATTKeyword.ParameterValueType.STRING,
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
                    this.filePath, data);
        }
        return super.getDescription() + "<br>" + message;
    }

}
