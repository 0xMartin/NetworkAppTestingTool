package utb.fai.natt.keyword.General;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

/**
 * Umoznuje definovat cekani
 */
@NATTAnnotation.Keyword(
    name = "wait", 
    description = "Pauses the test execution for a defined duration.", 
    parameters = { "time_out" }, 
    types = { ParamValType.LONG },
    kwGroup = "NATT General"
    )
public class WaitKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(WaitKw.class);

    protected Long waitTimeMs;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // musi byt vyssi jak 0
        if (this.waitTimeMs <= 0) {
            throw new InternalErrorException("Wait time must be higher than 0 ms!");
        }
        // limitace na max 20 minut cekání
        if (this.waitTimeMs > 20 * 60 * 1000) {
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
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (long) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "time_out", NATTKeyword.DEFAULT_PARAMETER_NAME },
                NATTKeyword.ParamValType.LONG,
                true);
        waitTimeMs = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

}
