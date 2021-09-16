package hr.fer.zemris.java.hw11.jnotepadpp.local;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provider designed as singleton object which reads property files given in
 * package hr.fer.zemris.java.hw11.jnotepadpp which start with word "trans".
 * Initial language is English.
 * @author Luka Kraljević
 *
 */
public class LocalizationProvider extends AbstractLocalizationProvider {
    
    /**
     * Currently set language.
     */
    private String language;
    
    /**
     * ResourceBundle which reads all pairs of keys and expressions in all
     * languages provided and read in property files.
     */
    private ResourceBundle bundle;
    
    /**
     * Instance of this object which will be instantiated only once and then
     * only returned.
     */
    private static final LocalizationProvider provider=new LocalizationProvider();
    
    /**
     * Sets initial parameters of the provider.
     */
    private LocalizationProvider() {
        language="en";
        bundle=ResourceBundle.getBundle("hr.fer.zemris.java.hw11.jnotepadpp.trans"
                , Locale.forLanguageTag(language));
        
    }
    
    /**
     * Returns instance of this object.
     * @return this provider
     */
    public static LocalizationProvider getInstance() {
        return provider;
    }
    

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }
    
    /**
     * Sets the language of this provider.
     * @param language language given as shortage, e.g. "en" or "de"
     */
    public void setLanguage(String language) {
        this.language=language;
        bundle=ResourceBundle.getBundle("hr.fer.zemris.java.hw11.jnotepadpp.trans"
                , Locale.forLanguageTag(language));
        fire();
    }

}
