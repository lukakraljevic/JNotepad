package hr.fer.zemris.java.hw11.jnotepadpp.local;

/**
 * Provider bridge which provides that no window components of deregistered
 * window will be treated as utilizable objects, but as garbage to be collected.
 * @author Luka Kraljević
 *
 */
public class LocalizationProviderBridge extends AbstractLocalizationProvider {
    
    /**
     * Detects if a frame is connected to this bridge.
     */
    private boolean connected;
    
    /**
     * Listener which is registered for given frame.
     */
    private ILocalizationListener listener;
    
    /**
     * Provider which handles the registered listeners.
     */
    private ILocalizationProvider parent;
    
    /**
     * Initializes this bridge by taking parent provider.
     * @param parent Parent provider which handles the listeners.
     */
    public LocalizationProviderBridge(ILocalizationProvider parent) {
        this.parent = parent;
    }
    
    /**
     * Deregisters a frame from the bridge.
     */
    public void disconnect() {
        connected=false;
        parent.removeLocalizationListener(listener);
    }
    
    /**
     * Resgisters a frame to this bridge.
     */
    public void connect() {
        if (connected) {
            return;
        }
        
        connected=true;
        listener=new ILocalizationListener() {
            
            @Override
            public void localizationChanged() {
                fire();
            }
        };
        parent.addLocalizationListener(listener);
    }

    @Override
    public String getString(String key) {
        return parent.getString(key);
    }
    
    

}
