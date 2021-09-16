package hr.fer.zemris.java.hw11.jnotepadpp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import hr.fer.zemris.java.hw11.jnotepadpp.local.FormLocalizationProvider;
import hr.fer.zemris.java.hw11.jnotepadpp.local.LJLabel;
import hr.fer.zemris.java.hw11.jnotepadpp.local.LocalizableAction;
import hr.fer.zemris.java.hw11.jnotepadpp.local.LocalizationProvider;

/**
 * Implementation of high-level text editor similar to Notepad++ on Windows or
 * Gedit on Linux. The editor offers options such as creating new document,
 * opening existing document, saving, saving-as, cutting/copying/pasting text
 * and statistics. It also supports i18n in three different languages: English,
 * Croatian and German.
 * 
 * @author Luka Kraljević
 *
 */
public class JNotepadPP extends JFrame {

    /**
     * Default serial version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Tabs where user can switch text areas in different documents.
     */
    private JTabbedPane tabs;

    /**
     * Localization provider to support easy switch of languages in the app.
     */
    private FormLocalizationProvider flp = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);;

    /**
     * Path of currently opened file or null, if the edited text area is not on
     * disk.
     */
    private Path openedFilePath;

    /**
     * Current editor which is active and being edited.
     */
    private JTextArea currEditor;

    /**
     * Buffer which stores data which is copied/cut and will eventually be
     * pasted.
     */
    private String buffer;

    /**
     * Icon of red floppy - signification for unsaved document.
     */
    private final ImageIcon redFloppy;

    /**
     * Icon of green floppy - signification for saved or unchanged document.
     */
    private final ImageIcon greenFloppy;

    /**
     * List of currently active and prepared editors.
     */
    private List<JTextArea> editors;

    /**
     * Localizable label for rendering message "length" in different languages.
     */
    private LJLabel len = new LJLabel("len", flp);

    /**
     * Renders total length of document.
     */
    private JLabel lenVal = new JLabel();

    /**
     * Label for current line.
     */
    private JLabel ln = new JLabel("Ln: ");

    /**
     * Label for current column.
     */
    private JLabel col = new JLabel("Col: ");

    /**
     * Label for number of selected characters or 0 if there is no selection.
     */
    private JLabel sel = new JLabel("Sel: ");

    /**
     * String which stores current language.
     */
    private String currLang = "en";

    /**
     * Label which renders current date and time.
     */
    private CurrentTime time = new CurrentTime();

    /**
     * Instance which stores all action of this app.
     */
    private JNotepadPPActions actions;

    /**
     * Constructs the frame and all neccessary info to start the editor.
     */
    public JNotepadPP() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(600, 600);
        redFloppy = loadImage("icons/floppy_disk_red.png");
        greenFloppy = loadImage("icons/floppy_disk_green.png");
        editors = new ArrayList<>();
        actions = new JNotepadPPActions(this);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                int index = tabs.getSelectedIndex();

                if (checkReadyToClose()) {
                    dispose();
                } else {
                    tabs.setSelectedIndex(index);
                }
                time.stop();

            }
        });

        initGUI();
        setLocationRelativeTo(null);
    }
    
    /**
     * The rest of app's initialization and all items' and actions' instatation.
     */
    private void initGUI() {
        setTitle("JNotepad++");
        tabs = new JTabbedPane();
        tabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabs.getSelectedIndex();
                if (index == -1) {
                    createBlankOpen("new", null);
                    return;
                }
                
                String title = "JNotepad++";

                if (tabs.getTitleAt(index).equals("new")) {
                    openedFilePath = null;
                } else {
                    String path=tabs.getToolTipTextAt(index);
                    openedFilePath = Paths.get(path);
                    title = openedFilePath.toAbsolutePath().toString() + " - " + title;
                }

                JNotepadPP.this.setTitle(title);
                currEditor = editors.get(index);
            }
        });

        JPanel tabContent = new JPanel(new BorderLayout());
        JPanel bottom = new JPanel(new GridLayout(1, 3));

        JPanel start = new JPanel();
        start.setLayout(new BoxLayout(start, BoxLayout.X_AXIS));
        len.setHorizontalAlignment(SwingConstants.LEADING);
        lenVal.setHorizontalAlignment(SwingConstants.LEADING);
        start.add(len);
        start.add(Box.createRigidArea(new Dimension(5,0)));
        start.add(lenVal);
        start.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        bottom.add(start);
        
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        middle.add(ln);
        middle.add(Box.createRigidArea(new Dimension(5,0)));
        middle.add(col);
        middle.add(Box.createRigidArea(new Dimension(5,0)));
        middle.add(sel);
        middle.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        bottom.add(middle);
        
        time.setHorizontalAlignment(SwingConstants.RIGHT);
        time.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        bottom.add(time);

        tabContent.add(tabs, BorderLayout.CENTER);
        tabContent.add(bottom, BorderLayout.PAGE_END);
        getContentPane().setLayout(new BorderLayout());
        createBlankOpen("new", null);
        getContentPane().add(tabContent, BorderLayout.CENTER);
        createActions();
        createMenus();
        createToolbars();
    }

    /**
     * Checks if the app is ready to close by asking user to save any unsaved
     * work.
     * 
     * @return true if the app is ready to close, false otherwise
     */
    boolean checkReadyToClose() {
        boolean saveConfirmed = false;
        int msg = -2;

        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.getIconAt(i).equals(redFloppy)) {
                if (!saveConfirmed) {
                    msg = JOptionPane.showConfirmDialog(JNotepadPP.this,
                            "Do you want to save all changed documents?", "Closing program",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    saveConfirmed = true;
                }

                if (msg == JOptionPane.CANCEL_OPTION) {
                    return false;
                } else if (msg == JOptionPane.YES_OPTION) {
                    TabComponent comp=(TabComponent) tabs.getTabComponentAt(i);
                    String lab=comp.getLabel();
                    if (lab.equals(flp.getString("new"))) {
                        openedFilePath = null;
                    } else {
                        openedFilePath = Paths.get(tabs.getToolTipTextAt(i));
                    }
                    currEditor = editors.get(i);

                    boolean result = false;
                    if (openedFilePath == null) {
                        actions.saveAs();
                    }

                    if (result) {
                        actions.saveDoc();
                    }
                }
                break;
            }
        }

        return true;

    }

    /**
     * Loads the icon from given path.
     * 
     * @param fileName
     *            name of file storing wanted icon
     * @return instance of ImageIcon which is the icon taken from given file
     */
    private ImageIcon loadImage(String fileName) {
        InputStream is = this.getClass().getResourceAsStream(fileName);
        if (is == null) {
            throw new IllegalArgumentException("The given file does not exist!");
        }

        byte[] bytes = readAllBytes(is);
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImageIcon(bytes);
    }

    /**
     * Reads the data from given input stream and returns this data in a byte
     * array.
     * 
     * @param is
     *            stream the data is read from
     * @return data stored in byte array
     */
    private byte[] readAllBytes(InputStream is) {
        byte[] data = new byte[1024];
        int read = 0;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        while (read != -1) {
            try {
                read = is.read(data, 0, data.length);
                if (read != -1) {
                    output.write(data, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output.toByteArray();
    }

    

    /**
     * Label for rendering current date and time.
     * 
     * @author Luka Kraljević
     *
     */
    static class CurrentTime extends JLabel {

        /**
         * Default serial version for serialisation.
         */
        private static final long serialVersionUID = 1L;

        /**
         * String which stores current data and time.
         */
        volatile String time;

        /**
         * Date time formatter.
         */
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        /**
         * Flag which indicates when the app is closed so that the thread for
         * updating time can be stopped.
         */
        volatile boolean stopRequested;

        /**
         * Initializes this date-time label.
         */
        public CurrentTime() {
            updateTime();
            setVisible(true);

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ex) {
                    }
                    if (stopRequested) {
                        break;
                    }
                    SwingUtilities.invokeLater(() -> {
                        updateTime();
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        }

        /**
         * Stops updating time.
         */
        private void stop() {
            stopRequested = true;
        }

        /**
         * Updates current date time and refreshes this label's content.
         */
        private void updateTime() {
            time = formatter.format(new Date());
            setText(time);
        }

    }

    /**
     * Returns listener for close button placed on every tab.
     * 
     * @param comp
     *            component placed as tab component which describes certain tab
     *            (in the title part).
     * @return listener for close button
     */
    ActionListener getCloseListener(TabComponent comp) {

        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabs.indexOfTabComponent(comp);
                int ans = -2;
                if (tabs.getIconAt(index).equals(redFloppy)) {
                    ans = JOptionPane.showConfirmDialog(
                            tabs.getComponentAt(index), 
                            "Do you want save all changes?",
                            "Save changes", 
                            JOptionPane.YES_NO_CANCEL_OPTION);
                }

                if (ans == JOptionPane.YES_OPTION) {
                    boolean result = actions.saveAs();
                    if (!result) {
                        return;
                    }

                    if (openedFilePath != null) {
                        actions.saveDoc();
                    }
                } else if (ans == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                tabs.remove(index);
                editors.remove(index);
            }
        };
    }

    /**
     * Creates blank document or opens existing document in this editor.
     * 
     * @param tabName
     *            if new document is being created, then tabName must be "new"
     *            (regardless of editor's language), otherwise it will be name
     *            of file
     * @param fullPath
     *            if new document is being created, then fullPath must be null,
     *            otherwise, it will be full path to opening file
     */
    protected void createBlankOpen(String tabName, Path fullPath) {
        JTextArea editor = new JTextArea();
        Document doc = editor.getDocument();

        editor.addCaretListener(new CaretListener() {

            /**
             * Old length of the file.
             */
            int oldLength;

            /**
             * Detects whether the old length has been initialized.
             */
            boolean updated = false;

            @Override
            public void caretUpdate(CaretEvent e) {
                int index = tabs.getSelectedIndex();
                int docLen = doc.getLength();

                if (!updated) {
                    oldLength = docLen;
                    updated = true;
                }

                if (docLen != oldLength) {
                    tabs.setIconAt(index, redFloppy);
                    boolean createNew=false;
                    if (tabs.getTitleAt(index).equals("new")) {
                        createNew=true;
                    }
                    
                    TabComponent comp = new TabComponent(
                            JNotepadPP.this, tabs, redFloppy, createNew);
                    tabs.setTabComponentAt(index, comp);

                }

                oldLength = docLen;

                if (Math.abs(e.getDot() - e.getMark()) == 0) {
                    toUpper.setEnabled(false);
                    toLower.setEnabled(false);
                    invert.setEnabled(false);
                } else {
                    toUpper.setEnabled(true);
                    toLower.setEnabled(true);
                    invert.setEnabled(true);
                }

                lenVal.setText(Integer.toString(docLen));

                try {
                    int currLine = editor.getLineOfOffset(e.getDot());
                    int lineStart = editor.getLineStartOffset(currLine);
                    ln.setText("Ln: " + currLine);
                    col.setText("Col: " + (e.getDot() - lineStart));
                    sel.setText("Sel: " + Math.abs(e.getDot() - e.getMark()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }

            }
        });

        editors.add(editor);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(editor));
        
        boolean createNew = false;
        if (tabName.equals("new")) {
            createNew = true;
        }
        
        TabComponent tabPane = new TabComponent(this, tabs, 
                                            greenFloppy, createNew);
        
        tabs.addTab(tabName, panel);
        int index=tabs.getTabCount() - 1;
        if (fullPath != null) {
            tabs.setToolTipTextAt(index, fullPath.toString());
            openedFilePath = fullPath;
        } else {
            openedFilePath = null;
        }
        
        tabs.setIconAt(index, greenFloppy);
        tabs.setTabComponentAt(index, tabPane);
        tabs.setSelectedIndex(index);
        currEditor = editor;
    }

    /**
     * Action for switching entire GUI to English language.
     */
    private Action en = new AbstractAction() {

        /**
         * Default serial version.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("en");
            currLang = "en";
        }
    };

    /**
     * Action for switching entire GUI to German language.
     */
    private Action de = new AbstractAction() {

        /**
         * Default serial version.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("de");
            currLang = "de";
        }
    };

    /**
     * Action for switching entire GUI to Croatian language.
     */
    private Action hr = new AbstractAction() {

        /**
         * Default serial version.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            LocalizationProvider.getInstance().setLanguage("hr");
            currLang = "hr";
        }
    };

    /**
     * Creates all supported actions in this editor.
     */
    private void createActions() {
        createBlankAction = actions.getCreateBlankAction();
        openDocumentAction = actions.getOpenDocumentAction();
        saveDocumentAction = actions.getSaveDocumentAction();
        saveAsDocumentAction = actions.getSaveAsDocumentAction();
        copyTextAction = actions.getCopyTextAction();
        pasteTextAction = actions.getPasteTextAction();
        cutTextAction = actions.getCutTextAction();
        statistics = actions.getStatistics();
        toUpperCase = actions.getToUpperCase();
        toLowerCase = actions.getToLowerCase();
        invertCase = actions.getInvertCase();
        ascending = actions.getAscending();
        descending = actions.getDescending();
        unique = actions.getUnique();
        exitAction = actions.getExitAction();

        createBlankAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
        createBlankAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);

        openDocumentAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        openDocumentAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);

        saveDocumentAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
        saveDocumentAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

        saveAsDocumentAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
        saveAsDocumentAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);

        cutTextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        cutTextAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);

        copyTextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
        copyTextAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);

        pasteTextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        pasteTextAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);

        statistics.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
        statistics.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);

        exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);

        en.putValue(Action.NAME, "en");
        hr.putValue(Action.NAME, "hr");
        de.putValue(Action.NAME, "de");

    }

    /**
     * Creates blank document and opens new tab for new editor.
     */
    private Action createBlankAction;

    /**
     * Opens document from the disk in the new tab.
     */
    private Action openDocumentAction;

    /**
     * Saves the document on the disk if it isn't saved before or just saves the
     * changes to the document.
     */
    private Action saveDocumentAction;

    /**
     * Saves the document on disk, but only if it wasn't saved before.
     */
    private Action saveAsDocumentAction;

    /**
     * Copies selected part of text.
     */
    private Action copyTextAction;

    /**
     * Pastes copied/cut text on the position of the caret.
     */
    private Action pasteTextAction;

    /**
     * Cuts the selected part of text.
     */
    private Action cutTextAction;

    /**
     * Prints in the info box number of letters, number of non-blank chars and
     * number of lines.
     */
    private Action statistics;

    /**
     * Changes all selected letters to uppercase.
     */
    private Action toUpperCase;

    /**
     * Changes all selected letters to lowercase.
     */
    private Action toLowerCase;

    /**
     * Inverts cases of all selected letters.
     */
    private Action invertCase;

    /**
     * Sorts all selected lines in the document in ascending order considering
     * currently set language. If selection affects only part of line, the whole
     * line is treated as selected.
     */
    private Action ascending;

    /**
     * Sorts all selected lines in the document in descending order considering
     * currently set language. If selection affects only part of line, the whole
     * line is treated as selected.
     */
    private Action descending;

    /**
     * Action for throwing out all duplicate lines from the document to keep
     * only the first appearance.
     */
    private Action unique;

    /**
     * Action for exiting the application.
     */
    private Action exitAction;

    /**
     * Menu item for action toUpperCase.
     */
    private JMenuItem toUpper;

    /**
     * Menu item for action toLowerCase.
     */
    private JMenuItem toLower;

    /**
     * Menu item for action invertCase
     */
    private JMenuItem invert;

    /**
     * Creates all supported menus in the text editor, such as file menu, menu
     * for editing, information, toggleing languages and different tools.
     */
    private void createMenus() {

        toUpper = new JMenuItem(toUpperCase);
        toLower = new JMenuItem(toLowerCase);
        invert = new JMenuItem(invertCase);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(new LocalizableAction("File", null, flp));
        menuBar.add(fileMenu);
        fileMenu.add(new JMenuItem(createBlankAction));
        fileMenu.add(new JMenuItem(openDocumentAction));
        fileMenu.add(new JMenuItem(saveDocumentAction));
        fileMenu.add(new JMenuItem(saveAsDocumentAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(exitAction));

        JMenu editMenu = new JMenu(new LocalizableAction("Edit", null, flp));
        editMenu.add(new JMenuItem(copyTextAction));
        editMenu.add(new JMenuItem(pasteTextAction));
        editMenu.add(new JMenuItem(cutTextAction));
        menuBar.add(editMenu);

        JMenu infoMenu = new JMenu(new LocalizableAction("Info", null, flp));
        infoMenu.add(new JMenuItem(statistics));
        menuBar.add(infoMenu);

        JMenu languages = new JMenu(new LocalizableAction("Languages", null, flp));
        languages.add(new JMenuItem(en));
        languages.add(new JMenuItem(de));
        languages.add(new JMenuItem(hr));
        menuBar.add(languages);

        JMenu toolsMenu = new JMenu(new LocalizableAction("Tools", null, flp));

        JMenu changeCase = new JMenu(new LocalizableAction("chCase", null, flp));

        toUpper.setEnabled(false);
        toLower.setEnabled(false);
        invert.setEnabled(false);

        changeCase.add(toUpper);
        changeCase.add(toLower);
        changeCase.add(invert);
        toolsMenu.add(changeCase);

        JMenu sort = new JMenu(new LocalizableAction("sort", null, flp));
        sort.add(new JMenuItem(ascending));
        sort.add(new JMenuItem(descending));
        toolsMenu.add(sort);

        toolsMenu.add(new JMenuItem(unique));
        menuBar.add(toolsMenu);

        this.setJMenuBar(menuBar);
    }

    /**
     * Creates toolbar for actions supported in file, edit and info menu.
     */
    private void createToolbars() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(true);
        toolBar.add(new JButton(createBlankAction));
        toolBar.add(new JButton(openDocumentAction));
        toolBar.add(new JButton(saveDocumentAction));
        toolBar.add(new JButton(saveAsDocumentAction));
        toolBar.addSeparator();
        toolBar.add(new JButton(copyTextAction));
        toolBar.add(new JButton(pasteTextAction));
        toolBar.add(new JButton(cutTextAction));
        toolBar.addSeparator();
        toolBar.add(new JButton(statistics));
        toolBar.addSeparator();
        toolBar.add(new JButton(exitAction));

        this.getContentPane().add(toolBar, BorderLayout.PAGE_START);

    }

    /**
     * @return the tabs
     */
    public JTabbedPane getTabs() {
        return tabs;
    }

    /**
     * @return the flp
     */
    public FormLocalizationProvider getFlp() {
        return flp;
    }

    /**
     * @return the openedFilePath
     */
    public Path getOpenedFilePath() {
        return openedFilePath;
    }

    /**
     * @return the currEditor
     */
    public JTextArea getCurrEditor() {
        return currEditor;
    }

    /**
     * @return the buffer
     */
    public String getBuffer() {
        return buffer;
    }

    /**
     * @return the redFloppy
     */
    public ImageIcon getRedFloppy() {
        return redFloppy;
    }

    /**
     * @return the greenFloppy
     */
    public ImageIcon getGreenFloppy() {
        return greenFloppy;
    }

    /**
     * @return the currLang
     */
    public String getCurrLang() {
        return currLang;
    }

    /**
     * @param buffer
     *            the buffer to set
     */
    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    /**
     * @param openedFilePath
     *            the openedFilePath to set
     */
    public void setOpenedFilePath(Path openedFilePath) {
        this.openedFilePath = openedFilePath;
    }
    
    /**
     * Main method of the application.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JNotepadPP().setVisible(true);
        });
    }

}
