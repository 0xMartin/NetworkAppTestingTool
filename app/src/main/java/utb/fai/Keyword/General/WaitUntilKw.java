package utb.fai.Keyword.General;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import utb.fai.Core.Keyword;
import utb.fai.Core.MessageListener;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.NATTModule;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat akci cekani na udalos v podobe prijeti zpravy danym
 * module.
 */
@NATTAnnotation.Keyword(name = "wait_until")
public class WaitUntilKw extends Keyword {

    /**
     * Obsah zpravy ktera vyvolala akci na kterou cekala tato keyword bude ulozen do teto promenne <module-name>-action-msg
     */
    public static final String VAR_ACTION_MSG_POSTFIX = "-action-msg";

    private NATTLogger logger = new NATTLogger(WaitUntilKw.class);

    protected String moduleName;
    protected Long timeOut;

    private long elapsedTime;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        this.elapsedTime = -1;

        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        // timeout
        long timeoutMillis = (timeOut == null) ? 10000 : timeOut;
        if (timeoutMillis <= 0) {
            throw new InternalErrorException("Timeout must be higher than 0 ms!");
        }
        // limitace na max 20 minut cekání
        if(timeoutMillis > 20 * 60 * 1000) {
            throw new InternalErrorException("Max value of timeout is 20 min!");
        }

        // ziska nazvy modulu na jejihz akci ma cekat
        String[] moduleNames = this.moduleName.split("&");

        // definic action listeneru
        final Map<String, CountDownLatch> latchMap = new HashMap<>();
        for (String mName : moduleNames) {
            mName = mName.trim();
            NATTModule module = NATTContext.instance().getModule(mName);
            if (module == null) {
                return false;
            }
            CountDownLatch latch = new CountDownLatch(1);
            module.addMessageListener(new MessageListener() {
                @Override
                public void onMessageReceived(String sender, String tag, String message) {
                    if (latch.getCount() > 0) {
                        logger.info(String.format("Action triggered from module '%s'", sender));
                        // prijatou zpravu vlozi do promenne
                        NATTContext.instance().storeValueToVariable(sender + WaitUntilKw.VAR_ACTION_MSG_POSTFIX, message);
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("module_name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // time_out (long) [neni vyzadovany]
        val = this.getParameterValue("time_out", Keyword.ParameterValueType.LONG,
                false);
        timeOut = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
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
