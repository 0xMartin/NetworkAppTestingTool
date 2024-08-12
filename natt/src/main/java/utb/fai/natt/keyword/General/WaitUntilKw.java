package utb.fai.natt.keyword.General;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.IMessageListener;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje definovat akci cekani na udalos v podobe prijeti zpravy danym
 * module.
 */
@NATTAnnotation.Keyword(
    name = "wait_until",
    description = "It waits until a message is received from a certain communication module. Messages can be filtered using the keyword create_filter_action. The content of the message that triggered the action is automatically saved in the (module-name)-action-msg variable for possible testing.",
    parameters = {"module_name", "time_out"},
    types = {ParamValType.STRING, ParamValType.LONG},
    kwGroup = "NATT General"
    )
public class WaitUntilKw extends NATTKeyword {

    /**
     * Obsah zpravy ktera vyvolala akci na kterou cekala tato keyword bude ulozen do
     * teto promenne <module-name>-action-msg
     */
    public static final String VAR_ACTION_MSG_POSTFIX = "-action-msg";

    private NATTLogger logger = new NATTLogger(WaitUntilKw.class);

    protected String moduleName;
    protected Long timeOut;

    private long elapsedTime;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        this.elapsedTime = -1;

        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        // timeout
        long timeoutMillis = (timeOut == null) ? 10000 : timeOut;
        if (timeoutMillis <= 0) {
            throw new InternalErrorException("Timeout must be higher than 0 ms!");
        }
        // limitace na max 20 minut cekání
        if (timeoutMillis > 20 * 60 * 1000) {
            throw new InternalErrorException("Max value of timeout is 20 min!");
        }

        // ziska nazvy modulu na jejihz akci ma cekat
        String[] moduleNames = this.moduleName.split("&");

        // definic action listeneru
        final Map<String, CountDownLatch> latchMap = new HashMap<>();
        for (String mName : moduleNames) {
            mName = mName.trim();
            NATTModule module = ctx.getActiveModule(mName);
            if (module == null) {
                return false;
            }
            CountDownLatch latch = new CountDownLatch(1);
            module.addMessageListener(new IMessageListener() {
                @Override
                public void onMessageReceived(String sender, String tag, String message) {
                    if (latch.getCount() > 0) {
                        logger.info(String.format("Action triggered from module '%s'", sender));
                        // prijatou zpravu vlozi do promenne
                        NATTContext.instance().storeValueToVariable(sender + WaitUntilKw.VAR_ACTION_MSG_POSTFIX,
                                message);
                    }
                    latch.countDown();
                }
            });
            latchMap.put(mName, latch);
        }

        // ceka na vyvolani akci od vsech modulu
        logger.info(String.format("Waiting for action. Actions count: %d, Time out: %d ms", moduleNames.length,
                this.timeOut));
        long startTime = System.currentTimeMillis();
        for (CountDownLatch latch : latchMap.values()) {
            try {
                if (!latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
                    logger.warning("Timeout.. Action was not invoked!");
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.elapsedTime = (System.currentTimeMillis() - startTime);
        logger.info("Action was invoked. Elapsed time: " + elapsedTime + " ms");
        return true;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module_name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();

        // time_out (long) [neni vyzadovany]
        val = this.getParameterValue("time_out", NATTKeyword.ParamValType.LONG,
                false);
        timeOut = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String message;
        if (this.elapsedTime != -1) {
            message = String.format("<font color=\"green\">Action was invoked. Elapsed time: <b>%d</b> ms</font>",
                    this.elapsedTime);
        } else {
            message = "<font color=\"red\">Timeout.. Action was not invoked!</font>";
        }
        return super.getDescription() + "<br>" + message;
    }

}
