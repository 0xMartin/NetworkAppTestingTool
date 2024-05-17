package utb.fai.Keyword.General;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTLogger;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat cekani
 */
@NATTAnnotation.Keyword(name = "wait")
public class WaitKw extends Keyword {

    private NATTLogger logger = new NATTLogger(WaitKw.class);

    protected Long waitTimeMs;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // musi byt vyssi jak 0
        if (this.waitTimeMs <= 0) {
            throw new InternalErrorException("Wait time must be higher than 0 ms!");
        }
        // limitace na max 20 minut cekání
        if(this.waitTimeMs > 20 * 60 * 1000) {
            throw new InternalErrorException("Max value of wait time is 20 min!");
        }
        
        try {
            logger.info("Waiting: " + waitTimeMs + " ms");
            Thread.sleep(waitTimeMs);
        } catch (InterruptedException e) {
            throw new InternalErrorException("Failed to sleep thread!");
        }

        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (long) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.LONG,
                true);
        waitTimeMs = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

}
