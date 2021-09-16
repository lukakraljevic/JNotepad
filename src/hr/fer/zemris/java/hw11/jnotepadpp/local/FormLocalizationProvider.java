package hr.fer.zemris.java.hw11.jnotepadpp.local;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Registres one given JFrame and deregisters it when the frame is closed.
 * @author Luka Kraljević
 *
 */
public class FormLocalizationProvider extends LocalizationProviderBridge {
    
    /**
     * Initializes whole localization structure and takes a frame to be
     * registered to the provider.
     * @param parent Parent provider with all functionalities.
     * @param frame JFrame to be added to this provider's listeners
     */
    public FormLocalizationProvider(ILocalizationProvider parent, JFrame frame) {
        super(parent);
        
        connect();
        
        frame.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }

}
