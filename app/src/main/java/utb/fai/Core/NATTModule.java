package utb.fai.Core;

import java.util.ArrayList;
import java.util.List;

import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Abstraktni trida pro zakladni pristupovani k modulu
 */
public abstract class NATTModule {

    /**
     * Umoznuje nastavit filtrovani akci
     */
    public static class MessageFilter {
        /** Pozadovany text, ktery se bude ve zprave v urcite forme nachazet */
        public String text;

        /** Pozadovany tag zpravy. V pripade ignorovani nastavit na null */
        public String tag;

        /** Mod vyhledavani textu ve zpravy: equals, contains, startswith, endswith */
        public String mode;

        /** True v pripade casesensitive. Defaultni hodnota je true. */
        public Boolean caseSensitive;

        public MessageFilter(String text, String tag, String mode, Boolean caseSensitive) {
            this.text = text;
            this.tag = tag;
            this.mode = mode;
            this.caseSensitive = caseSensitive;
        }
    }

    // obsahuje nazev modulu (typ)
    private String moduleTypeName;

    // obsahuje nazev objektu. pro odliseni vice stejnech modulu.
    private String name;

    // je true v pripade ze modul je uz spusteny
    private boolean isRunning;

    // list message listeneru
    private List<MessageListener> messageListeners = new ArrayList<MessageListener>();

    // list filtru
    private List<MessageFilter> filters = new ArrayList<MessageFilter>();

    public NATTModule(String name) throws NonUniqueModuleNamesException, InternalErrorException {
        if (name == null) {
            throw new InternalErrorException("Name of module is null");
        }
        if (name.isEmpty()) {
            throw new InternalErrorException("Name of module is empty");
        }

        // jmeno modulu
        this.name = name;

        this.isRunning = false;

        // jmeno typu modulu
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(NATTAnnotation.Module.class)) {
            NATTAnnotation.Module annotation = clazz.getAnnotation(NATTAnnotation.Module.class);
            this.moduleTypeName = annotation.value();
        }

        // vytvoreni bufferu zprav pro tento modul
        NATTContext.instance().getMessageBuffer().createMessageBufferForModule(this.name);

        // vlozi modul do listu aktivnich modulu v kontextu
        this.addToListIfNameIsUnique(NATTContext.instance().getModules());
    }

    /**
     * Ziska jmeno tohoto modulu. Jedna se o jmeno, ktere odlisuje stejne tento
     * modul od ostatnich
     * 
     * @return navez modulu
     */
    public String getName() {
        return name;
    }

    /**
     * Overi zda je modul aktivni
     * 
     * @return True v pripade ze modul je aktivni
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Nastavit stat o tom zde je modul aktivni nebo ne
     * 
     * @param isRunning Stat
     */
    protected void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * Ziska nazev typu modulu. Jedna ze o nazev jeho typu
     * 
     * @return Nazev typu modulu
     */
    public String getModuleTypeName() {
        return moduleTypeName;
    }

    /**
     * Navrati list fitru akci
     * 
     * @return List fitru
     */
    public List<MessageFilter> getActionFilterList() {
        return this.filters;
    }

    /**
     * Notifikuje vsechny naslouchajici tridy o prijeti zpravy timto modulem
     * 
     * @param tag     Tag "oznaceni" zpravy
     * @param message Obsah zpravy v textove podobe
     */
    protected void notifyMessageListeners(String tag, String message) {
        // filtrovani
        if (!this.filters.isEmpty()) {
            for (MessageFilter f : this.filters) {
                if (f.tag != null) {
                    if (!f.tag.equals(tag)) {
                        return;
                    }
                }
                if (!NATTAssert.assertCondition(
                        message,
                        f.text,
                        f.mode,
                        f.caseSensitive == null ? true : f.caseSensitive)) {
                    return;
                }
            }
        }

        // vyvolani akci
        for (MessageListener listener : messageListeners) {
            if (listener != null) {
                listener.onMessageReceived(this.getName(), tag, message);
            }
        }
    }

    /**
     * Prida message listener
     * 
     * @param listener MessageListener
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * Odstrani message listner
     * 
     * @param listener MessageListener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public String getNameForLogger() {
        return "(" + this.getName() + ") ";
    }

    /**
     * Abstraktni metoda pro sputeni modulu. Nyni vyuzivana pro spusteni modulu
     * "app.std.in", pro ten je vyhrazena zlavstni metoda.
     * 
     * @throws InternalErrorException
     */
    public abstract void runModule() throws InternalErrorException;

    /**
     * Abstraktni metoda pro ukonceni modulu. Vola se vzdy pro vsechny aktivovane
     * moduly kdyz uz doslo k dokonceni vykonavani urcite testovaci casti kde byl
     * tento modul definovan. Napriklad pokud byl definovat v ramci jednoho test
     * case tak na jeho konci bude take ukoncen.
     * 
     * @return True v pripade uspesneho ukonceni modulu
     */
    public abstract boolean terminateModule();

    /**
     * Odesle zpravu testovane aplikace pomoci tohoto modulu
     * 
     * @return True v pripade uspesneho odeslani zpravy na server. Neni nijak
     *         ovlivneno pripadnou neuspesnosti zpravy na komunikujici protistrane.
     *         Pouze urcuje zda se podarilo nebo nepodarilo zpravu odeslat.
     * @throws InternalErrorException
     */
    public abstract boolean sendMessage(String message) throws InternalErrorException;

    /**
     * Vlozi tento modul do predlozeneho listu modulu pokud v ramci listu je jeho
     * jmeno unikatni
     * 
     * @param modules Reference na list modulu
     * 
     * @throws NonUniqueModuleNamesException
     */
    private final void addToListIfNameIsUnique(List<NATTModule> modules) throws NonUniqueModuleNamesException {
        for (NATTModule module : modules) {
            if (module.getName().equals(this.name)) {
                throw new NonUniqueModuleNamesException(this.name);
            }
        }
        modules.add(this);
    }

}
