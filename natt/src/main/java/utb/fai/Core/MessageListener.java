package utb.fai.Core;

/**
 * Rozhrani pro message listener. Je vyuzivano pro naslouchani prichozich zprav
 * v libovolnem modulu.
 */
public interface MessageListener {

    /**
     * Zprava byla prijata modulem
     * 
     * @param sender  Jmeno modulu, ktery tuto zpravu prijal
     * @param tag     Tag "oznaceni" zpravy
     * @param message Obsah zpravy textove podobe
     */
    void onMessageReceived(String sender, String tag, String message);

}
