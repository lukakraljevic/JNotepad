package hr.fer.zemris.java.hw11.jnotepadpp.local;

/**
 * Provider which enables i18n in an application and easy switching
 * languages. It follows the observator pattern where all interested
 * observes must be registered to this provider.
 * @author Luka Kraljević
 *
 */
public interface ILocalizationProvider {
    
    /**
     * Adds listener to this provider.
     * @param listener listener to be registered 
     */
    public void addLocalizationListener(ILocalizationListener listener);
    
    /**
     * Removes listener from this provider.
     * @param listener listener to be deregistered
     */
    public void removeLocalizationListener(ILocalizationListener listener);
    
    /**
     * Gets the string under given key in properties file.
     * @param key key for word or expression which is used to substitute
     * this expression in any supported language.
     * @return string under given key at current language
     */
    public String getString(String key);

}
