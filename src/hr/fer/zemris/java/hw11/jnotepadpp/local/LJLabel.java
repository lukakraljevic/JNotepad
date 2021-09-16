package hr.fer.zemris.java.hw11.jnotepadpp.local;

import javax.swing.JLabel;

/**
 * Represents extended version of JLabel which provides instant change
 * of text with the change of localization.
 * @author Luka Kraljević
 *
 */
public class LJLabel extends JLabel {
    
    /**
     * Default serial version.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Key which determines text in this label.
     */
    private String key;
    
    /**
     * Initializator for this label's key and provider.
     * @param key Key which determines text in this label.
     * @param provider Localization provider for this label.
     */
    public LJLabel(String key, ILocalizationProvider provider) {
        this.key=key;
        
        updateLabel(provider.getString(key));
        
        provider.addLocalizationListener(new ILocalizationListener() {
            
            @Override
            public void localizationChanged() {
                updateLabel(provider.getString(LJLabel.this.key));
            }
        });
    }
    
    /**
     * Updates the content of this label.
     * @param string new content of the label
     */
    private void updateLabel(String string) {
        setText(string);
    }

}
