package utb.fai.natt.core;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import utb.fai.natt.spi.IMessageBuffer;
import utb.fai.natt.spi.INATTMessage;
import utb.fai.natt.spi.NATTLogger;

/**
 * Trida obsahuje implementaci bufferu zprav, ktery je vyuzivan vsemi moduly
 * tohoto nastroje. Pro kazdy modul je v teto tride vytvoren separatni buffer
 * zprav "list", do ktereho moduly ukladaji sve prichozi zpravy. Pred ukladanim
 * zprav je nejdrive nutne tento buffer vytvorit (to uz zajistuje abstraktni
 * trida NATTModul)
 */
public class MessageBuffer implements IMessageBuffer {

    /**
     * Posledni vlozena zprava do bufferu zprav bude zaroven ulozena do prommene s
     * nazvem <module-name>-last-msg
     */
    public static final String VAR_LAST_MSG_POSTFIX = "-last-msg";

    private NATTLogger logger = new NATTLogger(MessageBuffer.class);

    private ConcurrentHashMap<String, CopyOnWriteArrayList<INATTMessage>> messages;

    public MessageBuffer() {
        this.messages = new ConcurrentHashMap<String, CopyOnWriteArrayList<INATTMessage>>();
    }

    /**
     * Pro dany modul vytvori buffer pro zpravy. Buffer bude vytvoren jen pokud bude
     * specifikovane nejake jmeno a takove ktere bude unikatni.
     * 
     * @param name Jmeno modulu, pro ktery bude vytvoren buffer pro zpravy
     * @return True v pripade uspesneho vytvoreni
     */
    public boolean createMessageBufferForModule(String name) {
        if (name == null) {
            return false;
        }
        if (name.isEmpty()) {
            return false;
        }
        if (this.messages.get(name) != null) {
            return false;
        }

        this.messages.put(name, new CopyOnWriteArrayList<INATTMessage>());
        logger.info("Message buffer created for module with name: " + name);
        return true;
    }

    /**
     * Vlozi zpravu do bufferu. Obsah zpravy bude zaroven take ulozen do promenne s
     * nazvem <module-name>-last-msg. V teto promenne se tak bude nachazet vzdy
     * posledni prijata zprava.
     * 
     * @param name    Jmeno modulu, ktery zpravu vklada do bufferu
     * @param tag     Tag zpravy
     * @param message Obsah samotne zpravy
     */
    public boolean addMessage(String name, String tag, String message) {
        if (name.isEmpty()) {
            return false;
        }

        CopyOnWriteArrayList<INATTMessage> msgBuf = messages.get(name);
        if (msgBuf == null) {
            return false;
        }

        // vlozi zpravu do bufferu zprav
        msgBuf.add(new NATTMessage(tag, message));

        // vlozeni obsahu zpravy do promenne last message
        String varName = name + MessageBuffer.VAR_LAST_MSG_POSTFIX;
        varName = varName.replace(" ", "_");
        NATTContext.instance().getVariables().put(varName, message);

        logger.info(String.format("Message added to buffer [ Mod: %s | Tag: %s ] Content: '%s'", name, tag, message));
        return varName != null;
    }

    /**
     * Vycisti buffer zprav u vsech modulu
     */
    public void clearAll() {
        for (Map.Entry<String, CopyOnWriteArrayList<INATTMessage>> entry : this.messages.entrySet()) {
            CopyOnWriteArrayList<INATTMessage> moduleBuffer = entry.getValue();
            if (moduleBuffer != null) {
                moduleBuffer.clear();
            }
        }
        logger.info("Message buffer cleared");
    }

    /**
     * Vycisti buffer zprav u jednoho konkretniho modulu
     * 
     * @param name Jmeno modulu
     * 
     * @return True v pripade uspesneho vycisteni
     */
    public boolean clearOneMessageBuffer(String name) {
        CopyOnWriteArrayList<INATTMessage> moduleBuffer = this.messages.get(name);
        if (moduleBuffer == null) {
            return false;
        }

        moduleBuffer.clear();
        logger.info("Messages removed for modul with name: " + name);

        return true;
    }

    /**
     * Odstrani zpravu z bufferu pro dany modul
     * 
     * @param name    Jmeno modulu
     * @param message Reference na zpravu
     */
    public void removeMessageFromModule(String name, INATTMessage message) {
        CopyOnWriteArrayList<INATTMessage> messageList = messages.get(name);
        if (messageList != null) {
            messageList.remove(message);
            logger.info("One message removed from buffer [" + name + "|" + message.getTag() + "]");
        }
    }

    /**
     * Ziska vsechny zpravy, ktere prisli od konkretniho modulu. Tato metoda nikdy
     * nenavrati null. Pokud zadny zaznam v bufferu neni tak navratni prazdny list.
     * 
     * @param name Jmeno modulu
     * @return List zprav
     */
    public CopyOnWriteArrayList<INATTMessage> getMessages(String name) {
        return messages.getOrDefault(name, new CopyOnWriteArrayList<INATTMessage>());
    }

    /**
     * Zjisti zda buffer obsahuje zpravy od module se specifikovanym jmenem
     * 
     * @param name Jmeno modulu
     * @return True v pripade nalezeni zprav v bufferu pro dany modul
     */
    public boolean containsMessage(String name) {
        CopyOnWriteArrayList<INATTMessage> messageList = this.messages.get(name);
        if (messageList == null) {
            return false;
        }

        return !messageList.isEmpty();
    }

    /**
     * Vyhleda vsechny zpravy v bufferu, ktere splnuji pozadovane podminky. Pokud je
     * nejaky parametr prazny je ignorovan a jsou prijimany vsechny moznsti.
     * 
     * @param name          Jmeno modulu, ktery zpravu do bufferu vlozil
     * @param tag           Tag zpravy
     * @param searchText    Text, ktery je ve zprave hledany
     * @param searchType    Zpusob vyhledavani v textu zpravy
     * @param caseSensitive True pro case sensitive vyhledavani
     * @return Seznam zprava, ktere splnuji vsechny podminky. Tedy odpovida name,
     *         tag a take je nalezen odpovidajici textovy retezec v zprave.
     */
    public LinkedList<INATTMessage> searchMessages(String name, String tag, String searchText,
            INATTMessage.SearchType searchType,
            boolean caseSensitive) {
        if (tag == null) {
            tag = "";
        }
        if (searchText == null) {
            searchText = "";
        }

        LinkedList<INATTMessage> result = new LinkedList<INATTMessage>();

        for (Map.Entry<String, CopyOnWriteArrayList<INATTMessage>> entry : messages.entrySet()) {
            String key = entry.getKey();
            CopyOnWriteArrayList<INATTMessage> messageList = entry.getValue();
            if ((name.isEmpty() || key.equals(name))) {
                for (INATTMessage message : messageList) {
                    if (tag.isEmpty() || message.getTag().equals(tag)) {
                        if (searchText.isEmpty() || message.searchInMessage(searchText, searchType, caseSensitive)) {
                            result.add(message);
                        }
                    }
                }
            }
        }

        return result;
    }

}
