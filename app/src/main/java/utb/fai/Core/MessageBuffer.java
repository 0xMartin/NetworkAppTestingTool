package utb.fai.Core;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Trida obsahuje implementaci bufferu zprav, ktery je vyuzivan vsemi moduly
 * tohoto nastroje. Pro kazdy modul je v teto tride vytvoren separatni buffer
 * zprav "list", do ktereho moduly ukladaji sve prichozi zpravy. Pred ukladanim
 * zprav je nejdrive nutne tento buffer vytvorit (to uz zajistuje abstraktni
 * trida NATTModul)
 */
public class MessageBuffer {

    /**
     * Posledni vlozena zprava do bufferu zprav bude zaroven ulozena do prommene s nazvem <module-name>-last-msg
     */
    public static final String VAR_LAST_MSG_POSTFIX = "-last-msg";

    private NATTLogger logger = new NATTLogger(MessageBuffer.class);

    private ConcurrentHashMap<String, CopyOnWriteArrayList<NATTMessage>> messages;

    public MessageBuffer() {
        this.messages = new ConcurrentHashMap<String, CopyOnWriteArrayList<NATTMessage>>();
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

        this.messages.put(name, new CopyOnWriteArrayList<NATTMessage>());
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

        CopyOnWriteArrayList<NATTMessage> msgBuf = messages.get(name);
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
        for (Map.Entry<String, CopyOnWriteArrayList<NATTMessage>> entry : this.messages.entrySet()) {
            CopyOnWriteArrayList<NATTMessage> moduleBuffer = entry.getValue();
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
        CopyOnWriteArrayList<NATTMessage> moduleBuffer = this.messages.get(name);
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
    public void removeMessageFromModule(String name, NATTMessage message) {
        CopyOnWriteArrayList<NATTMessage> messageList = messages.get(name);
        if (messageList != null) {
            messageList.remove(message);
            logger.info("One message removed from buffer [" + name + "|" + message.tag + "]");
        }
    }

    /**
     * Ziska vsechny zpravy, ktere prisli od konkretniho modulu. Tato metoda nikdy
     * nenavrati null. Pokud zadny zaznam v bufferu neni tak navratni prazdny list.
     * 
     * @param name Jmeno modulu
     * @return List zprav
     */
    public CopyOnWriteArrayList<NATTMessage> getMessages(String name) {
        return messages.getOrDefault(name, new CopyOnWriteArrayList<NATTMessage>());
    }

    /**
     * Zjisti zda buffer obsahuje zpravy od module se specifikovanym jmenem
     * 
     * @param name Jmeno modulu
     * @return True v pripade nalezeni zprav v bufferu pro dany modul
     */
    public boolean containsMessage(String name) {
        CopyOnWriteArrayList<NATTMessage> messageList = this.messages.get(name);
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
    public LinkedList<NATTMessage> searchMessages(String name, String tag, String searchText, SearchType searchType,
            boolean caseSensitive) {
        if (tag == null) {
            tag = "";
        }
        if (searchText == null) {
            searchText = "";
        }

        LinkedList<NATTMessage> result = new LinkedList<NATTMessage>();

        for (Map.Entry<String, CopyOnWriteArrayList<NATTMessage>> entry : messages.entrySet()) {
            String key = entry.getKey();
            CopyOnWriteArrayList<NATTMessage> messageList = entry.getValue();
            if ((name.isEmpty() || key.equals(name))) {
                for (NATTMessage message : messageList) {
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

    /**
     * Typ vyhledavani
     */
    public enum SearchType {
        EQUALS,
        CONTAINS,
        STARTSWITH,
        ENDSWITH,
        NONE
    }

    /**
     * Jedna se o zpravu, ktera je prijata v libovolnem modulu od testovane
     * aplikace libovolnym zpusobem.
     */
    public static class NATTMessage {

        // Tag zprava nebo take jeji libovolne oznaceni
        private String tag;

        // Textovy obsah zpravy
        private String message;

        public NATTMessage(String tag, String message) {
            this.tag = tag;
            this.message = message;
        }

        public String getTag() {
            return tag;
        }

        public String getMessage() {
            return message;
        }

        /**
         * Pokusi se vyhledat text ve zprave podle zvoleneho vyhledavaciho pravidla.
         * 
         * @param searchText    Vyhledavani text
         * @param searchType    Zpusob hledani
         * @param caseSensitive True v pripade pro case sensitive hledani
         * @return True v pripade nalezeni textu se zvolenym zpusobem vyhledavani
         */
        public boolean searchInMessage(String searchText, SearchType searchType, boolean caseSensitive) {

            String target = this.message;
            if (!caseSensitive) {
                searchText = searchText.toLowerCase();
                target = target.toLowerCase();
            }

            switch (searchType) {
                case EQUALS:
                    return target.equals(searchText);
                case CONTAINS:
                    return target.contains(searchText);
                case STARTSWITH:
                    return target.startsWith(searchText);
                case ENDSWITH:
                    return target.endsWith(searchText);
                default:
                    return false;
            }
        }
    }

}
