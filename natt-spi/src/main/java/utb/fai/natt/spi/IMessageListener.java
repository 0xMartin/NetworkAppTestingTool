package utb.fai.natt.spi;

/**
 * Interface for a message listener. It is used to listen for incoming messages
 * in any module.
 */
public interface IMessageListener {

    /**
     * A message was received by the module.
     * 
     * @param sender  The name of the module that received this message.
     * @param tag     The tag or "label" of the message.
     * @param message The content of the message in text form.
     */
    void onMessageReceived(String sender, String tag, String message);

}
