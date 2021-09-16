package hr.fer.zemris.java.hw11.jnotepadpp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import hr.fer.zemris.java.hw11.jnotepadpp.local.LJLabel;

/**
 * Component which is located in the title part of every tab and shows
 * tab's title, icon and closing button.
 * @author Luka Kraljević
 *
 */
public class TabComponent extends JPanel {
    
    /**
     * Default serial version.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Label for displaying file names or signification for unsaved document.
     */
    private JLabel label;
    
    /**
     * Instantiates this tab component.
     * @param notepad JNotepadPP instance, instance of main editor
     * @param tabs apstraction of all tabs in the editor
     * @param icon icon ehich will be set to this component
     * @param createNew indicates if new blank document will be created
     */
    public TabComponent(JNotepadPP notepad, JTabbedPane tabs, ImageIcon icon, boolean createNew) {
        
        setOpaque(false);
        
        if (createNew) {
            label = new LJLabel("new", notepad.getFlp());
        } else {
            label = new JLabel() {
                /**
                 * Default serial version.
                 */
                private static final long serialVersionUID = 1L;
                
                @Override
                public String getText() {
                    int i = tabs.indexOfTabComponent(TabComponent.this);
                    if (i != -1) {
                        return tabs.getTitleAt(i);
                    }
                    return null;
                }
            };
            
        } 
        
        JLabel iconL=new JLabel(new ImageIcon(
                getScaledImage(icon.getImage(), 14, 14)));
        add(iconL);
        add(label);
        JButton button = new TabButton();
        button.addActionListener(notepad.getCloseListener(this));
        add(button);
        
    }
    
    /**
     * Returns current text of the label.
     * @return text in this component's label
     */
    public String getLabel() {
        return label.getText();
    }
    
    
    /**
     * Changes the size of given image to given parameters.
     * @param srcImg given image to be changed
     * @param w new width of the image
     * @param h new height of image
     * @return scaled image to given parameters
     */
    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
    
    /**
     * Button which has the funcionality of closing the tab.
     * @author Luka Kraljević
     *
     */
    private class TabButton extends JButton {
        
        /**
         * Default serial version.
         */
        private static final long serialVersionUID = 1L;
        
        /**
         * Initiates this button, it's size and functionality.
         */
        public TabButton() {
            int size = 16;
            setPreferredSize(new Dimension(size, size));
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            
            int offset = 6;
            g2.drawLine(offset, offset, getWidth() - offset - 1, getHeight() - offset - 1);
            g2.drawLine(getWidth() - offset - 1, offset, offset, getHeight() - offset - 1);
            g2.dispose();
        }
    }
}

