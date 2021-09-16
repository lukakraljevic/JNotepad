package hr.fer.zemris.java.hw11.jnotepadpp.local;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Represents action for all Swing components which support some kind
 * of action (e.g. buttons or menus). Enables easy communication of
 * Swing components and localization provider.
 * @author Luka Kraljević
 *
 */
public class LocalizableAction extends AbstractAction {
    
    /**
     * Default serial version.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Initializes this action ny taking name of key, description of funtionality
     * named with key and localization provider.
     * @param keyName key which is a substitution of name of action
     * @param keyDesc descritption of this action, if no description is provided, it
     * can be null
     * @param provider localization provider for this action
     */
    public LocalizableAction(String keyName, String keyDesc, ILocalizationProvider provider) {
        
        putValue(Action.NAME, provider.getString(keyName));
        if (keyDesc!=null) {
            putValue(Action.SHORT_DESCRIPTION, provider.getString(keyDesc));
        }
        provider.addLocalizationListener(new ILocalizationListener() {
            
            @Override
            public void localizationChanged() {
                putValue(Action.NAME, provider.getString(keyName));
                if (keyDesc!=null) {
                    putValue(Action.SHORT_DESCRIPTION, provider.getString(keyDesc));
                }
            }
        });
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }

}
