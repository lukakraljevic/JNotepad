package hr.fer.zemris.java.hw11.jnotepadpp.local;

/**
 * Listener which observes changes of language in the app so it can be
 * activated and perform all neccessary changes.
 * @author Luka Kraljević
 *
 */
public interface ILocalizationListener {
    
    /**
     * Method called to perform all changes caused by changing 
     * localization.
     */
    public void localizationChanged();

}
