package hr.fer.zemris.java.hw11.jnotepadpp.local;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ILocalizationProvider} which handles the job with
 * listeners, but not with providing texts under given keys.
 * @author Luka Kraljević
 *
 */
public abstract class AbstractLocalizationProvider implements ILocalizationProvider {
    
    /**
     * List of all registered listeners.
     */
    List<ILocalizationListener> listeners;
    
    /**
     * Initializes provider and instantiates list of listeners.
     */
    public AbstractLocalizationProvider() {
        listeners=new ArrayList<>();
    }

    @Override
    public void addLocalizationListener(ILocalizationListener listener) {
        if (listener==null) {
            return;
        } 
        listeners.add(listener);
        
    }

    @Override
    public void removeLocalizationListener(ILocalizationListener listener) {
        if (listener==null) {
            return;
        } 
        listeners.remove(listener);
    }
    
    /**
     * Notifies all listeners thah the change has occured.
     */
    public void fire() {
        for (ILocalizationListener list : listeners) {
            list.localizationChanged();
        }
    }


}
