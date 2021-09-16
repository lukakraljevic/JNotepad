package hr.fer.zemris.java.hw11.jnotepadpp;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import hr.fer.zemris.java.hw11.jnotepadpp.local.LocalizableAction;

/**
 * Storage of all supported action of JNotepadPP editor.
 * @author Luka Kraljević
 *
 */
public class JNotepadPPActions {
    
    /**
     * Given insance of JNotepadPP editor.
     */
    private JNotepadPP notepad;
    
    /**
     * Initializes the storage to connect this storage with currently
     * active JNotepadPP instance.
     * @param notepad currently active editor
     */
    public JNotepadPPActions(JNotepadPP notepad) {
        super();
        this.notepad = notepad;
    }
    
    /**
     * Saves the unsaved document to disk under specified name.
     * @return the state of saving the file, true if everything went fine,
     * otherwise false
     */
    protected boolean saveAs() {
        JFileChooser jfc=new JFileChooser();
        jfc.setDialogTitle("Save document");
        if (jfc.showSaveDialog(notepad) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(
                    notepad,
                    "Nothing is saved.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        Path sel=jfc.getSelectedFile().toPath();
        
        if (Files.exists(sel, LinkOption.NOFOLLOW_LINKS)) {
            JOptionPane.showMessageDialog(
                    notepad,
                    "File already exists!",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        notepad.setOpenedFilePath(sel);
        return true;
    }
    
    /**
     * Performs phisically saving data to the disk by writing them to
     * given path.
     */
    protected void saveDoc() {
        byte[] data = notepad.getCurrEditor().getText()
                .getBytes(StandardCharsets.UTF_8);
        try {
            Files.write(notepad.getOpenedFilePath(),  data);
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(
                 notepad,
                 "Error while saving file " + notepad.getOpenedFilePath(),
                 "Error",
                 JOptionPane.ERROR_MESSAGE
             );
            return;
        }
        
        JTabbedPane tabs=notepad.getTabs();
        int index=tabs.getSelectedIndex();
        tabs.setTitleAt(index, notepad.getOpenedFilePath()
                                      .getFileName()
                                      .toString());
        TabComponent tabCont=new TabComponent(
                notepad, 
                tabs, 
                notepad.getGreenFloppy(), 
                false
        );
        
        String toolTip=notepad.getOpenedFilePath()
                              .toAbsolutePath()
                              .toString();
        
        tabs.setToolTipTextAt(index, toolTip);
        tabs.setIconAt(index, notepad.getGreenFloppy());
        tabs.setTabComponentAt(index, tabCont);
    }
    
    /**
     * Returns create blank action described in JNotepadPP documentation.
     * @return the createBlankAction
     */
    public Action getCreateBlankAction() {
        return new LocalizableAction("New", "blankDes", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                notepad.createBlankOpen("new", null);
            }
        };
    }

    /**
     * Returns open document action described in JNotepadPP documentation.
     * @return the openDocumentAction
     */
    public Action getOpenDocumentAction() {
        return new LocalizableAction("Open","openDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open file");
                if (fc.showOpenDialog(notepad)!=JFileChooser.APPROVE_OPTION) {
                    return;
                }
                
                File fileName=fc.getSelectedFile();
                Path filePath = fileName.toPath();
                if (!Files.isReadable(filePath)) {
                    JOptionPane.showMessageDialog(notepad, 
                            "File " + fileName.getAbsolutePath() + 
                            " does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                byte[] bytes;
                try {
                    bytes=Files.readAllBytes(filePath);
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(
                            notepad,
                            "Error while reading file " + fileName.getAbsolutePath(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String text=new String(bytes, StandardCharsets.UTF_8);
                notepad.createBlankOpen(filePath.getFileName().toString(), 
                                        filePath.toAbsolutePath());
                notepad.getCurrEditor().setText(text);
                notepad.setOpenedFilePath(filePath);
            }
        };
    }

    /**
     * Returns save action described in JNotepadPP documentation.
     * @return the saveDocumentAction
     */
    public Action getSaveDocumentAction() {
        return new LocalizableAction("Save", "saveDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean result=true;
                
                if (notepad.getOpenedFilePath()==null) {
                    result=saveAs();
                }
                
                if (result) {
                    saveDoc();
                } else {
                    return;
                }
                
                
            }
        };
    }

    /**
     * Returns save as action described in JNotepadPP documentation.
     * @return the saveAsDocumentAction
     */
    public Action getSaveAsDocumentAction() {
        return new LocalizableAction("SaveAs", "saveAsDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                boolean result = saveAs();
                if (result) {
                    saveDoc();
                } else {
                    return;
                }
                
                JTabbedPane tabs=notepad.getTabs();
                int index=tabs.getSelectedIndex();
                tabs.setIconAt(index, notepad.getGreenFloppy());
                tabs.setTabComponentAt(index, new TabComponent(
                                notepad, tabs, notepad.getGreenFloppy(), false));
            }
        };
    }

    /**
     * Returns cut text action described in JNotepadPP documentation.
     * @return the cutTextAction
     */
    public Action getCutTextAction() {
        return new LocalizableAction("Cut", "cutDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                Document doc=currEditor.getDocument();
                int len=Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len==0) {
                    return;
                }
                int offset = Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                try {
                    notepad.setBuffer(doc.getText(offset, len));
                    doc.remove(offset, len);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                
            }
        };
    }

    /**
     * Returns copy text action described in JNotepadPP documentation.
     * @return the copyTextAction
     */
    public Action getCopyTextAction() {
        return new LocalizableAction("Copy", "copyDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                Document doc=currEditor.getDocument();
                int len=Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len==0) {
                    return;
                }
                int offset = Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                try {
                    notepad.setBuffer(doc.getText(offset, len));
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    /**
     * Returns paste text action described in JNotepadPP documentation.
     * @return the pasteTextAction
     */
    public Action getPasteTextAction() {
        return new LocalizableAction("Paste", "pasteDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                Document doc=currEditor.getDocument();
                int offset = currEditor.getCaret().getDot();
                
                try {
                    doc.insertString(offset, notepad.getBuffer(), null);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        };
    }

    /**
     * Returns statistics action described in JNotepadPP documentation.
     * @return the statistics
     */
    public Action getStatistics() {
        return new LocalizableAction("Stats", "statsDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                if (currEditor==null) {
                    return;
                }
                
                Document doc=currEditor.getDocument();
                
                int chars=doc.getLength();
                int nonBlank=chars;
                int lines=1;
                
                char[] text=null;
                try {
                    text = doc.getText(0, chars).toCharArray();
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                for (int i=0; i < chars; i++) {
                    if (Character.isWhitespace(text[i])) {
                        nonBlank--;
                    }
                    if (text[i]=='\n') {
                        lines++;
                    }
                }
                
                JOptionPane.showMessageDialog(notepad, 
                        String.format("Your document has %d characters, %d non-blank characters"
                        + " and %d lines.", chars,nonBlank,lines));
                
            }
        };
    }

    /**
     * Returns to uppercase action described in JNotepadPP documentation.
     * @return the toUpperCase
     */
    public Action getToUpperCase() {
        return new LocalizableAction("upper", "upperDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                if (currEditor==null) {
                    return;
                }
                Document doc=currEditor.getDocument();
                int len=Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len==0) {
                    return;
                }
                int offset = Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                try {
                    String text= doc.getText(offset, len);
                    doc.remove(offset, len);
                    doc.insertString(offset, text.toUpperCase(), null);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            };
            
        };
    }

    /**
     * Returns to lower case action described in JNotepadPP documentation.
     * @return the toLowerCase
     */
    public Action getToLowerCase() {
        return new LocalizableAction("lower", "lowerDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                if (currEditor==null) {
                    return;
                }
                Document doc=currEditor.getDocument();
                int len=Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len==0) {
                    return;
                }
                int offset = Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                try {
                    String text= doc.getText(offset, len);
                    doc.remove(offset, len);
                    doc.insertString(offset, text.toLowerCase(), null);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            };
            
        };
    }

    /**
     * Returns invert case action described in JNotepadPP documentation.
     * @return the invertCase
     */
    public Action getInvertCase() {
        return new LocalizableAction("invert", "invertDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                if (currEditor==null) {
                    return;
                }
                Document doc = currEditor.getDocument();
                int len = Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len == 0) {
                    return;
                } 
                
                int offset=Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                try {
                    String text= doc.getText(offset, len);
                    text=changeCase(text);
                    doc.remove(offset, len);
                    doc.insertString(offset, text, null);
                } catch(BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
            
            private String changeCase(String text) {
                char[] chars=text.toCharArray();
                for (int i =0; i < chars.length; i++) {
                    char c = chars[i];
                    if (Character.isLowerCase(c)) {
                        chars[i]=Character.toUpperCase(c);
                        
                    } else if (Character.isUpperCase(c)) {
                        chars[i]=Character.toLowerCase(c);
                    }
                }
                return new String(chars);
            }
        };
    }
    
    /**
     * Sorts lines by ascending or descending order, depending on
     * given parameter.
     * @param asc if true, sorting will be performed by ascending order,
     * otherwise descending
     * @return state of sorting performance, if anything went wrong, false
     * will be returned, otherwise true
     * @throws BadLocationException if bad location is given while editing
     * the document
     */
    private boolean performSorting(boolean asc) throws BadLocationException {
        JTextArea currEditor=notepad.getCurrEditor();
        if (currEditor==null) {
            return false;
        }
        Document doc = currEditor.getDocument();
        int len = Math.abs(currEditor.getCaret().getDot()-
                currEditor.getCaret().getMark());
        if (len == 0) {
            return false;
        } 
        
        int offset=Math.min(
                currEditor.getCaret().getDot(),
                currEditor.getCaret().getMark());
        
        List<String> lines=new ArrayList<>();
        int firstLine=currEditor.getLineOfOffset(offset);
        int lastLine=currEditor.getLineOfOffset(offset+len);
        
        for (int i=firstLine; i <= lastLine; i++) {
            int lineStart=currEditor.getLineStartOffset(i);
            int lineEnd=currEditor.getLineEndOffset(i);
            
            String elem=doc.getText(lineStart, lineEnd-lineStart);
            if (i == lastLine && !elem.contains("\n")) {
                elem+="\n";
            }
            lines.add(elem);
        }
        
        Locale locale = new Locale(notepad.getCurrLang());
        Collator collator = Collator.getInstance(locale);
        int factor;
        if (asc) {
            factor=1;
        } else {
            factor=-1;
        }
        
        Collections.sort(lines, 
                (s1,s2) -> factor*collator.compare(s1, s2));
        
        
        for (int i=firstLine; i <= lastLine; i++) {
            int lineStart=currEditor.getLineStartOffset(i);
            int lineEnd=currEditor.getLineEndOffset(i);
            doc.remove(lineStart, lineEnd-lineStart);
            doc.insertString(lineStart, lines.get(i-firstLine), null);
        }
        
        return true;
        
    }

    /**
     * Returns ascending action described in JNotepadPP documentation.
     * @return the ascending
     */
    public Action getAscending() {
        return new LocalizableAction("asc", "ascDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                
                try {
                    if (!performSorting(true)) {
                        return;
                    }
                    
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                
            };
        };
    }

    /**
     * Returns descending action described in JNotepadPP documentation.
     * @return the descending
     */
    public Action getDescending() {
        return new LocalizableAction("desc", "desDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!performSorting(false)) {
                        return;
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                
            };
        };
    }

    /**
     * Returns unique action described in JNotepadPP documentation.
     * @return the unique
     */
    public Action getUnique() {
        return new LocalizableAction("unique", "uniqueDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea currEditor=notepad.getCurrEditor();
                Document doc = currEditor.getDocument();
                int len = Math.abs(currEditor.getCaret().getDot()-
                        currEditor.getCaret().getMark());
                if (len == 0) {
                    return;
                } 
                
                int offset=Math.min(
                        currEditor.getCaret().getDot(),
                        currEditor.getCaret().getMark());
                
                
                List<String> lines=new ArrayList<>();
                try {
                    int firstLine=currEditor.getLineOfOffset(offset);
                    int lastLine=currEditor.getLineOfOffset(offset+len);
                    
                    for (int i=firstLine; i <= lastLine; i++) {
                        int lineStart=currEditor.getLineStartOffset(i);
                        int lineEnd=currEditor.getLineEndOffset(i);
                        
                        String line=doc.getText(lineStart, lineEnd-lineStart);
                        lines.add(line);
                        
                    }
                    
                    Locale locale = new Locale(notepad.getCurrLang());
                    Collator collator = Collator.getInstance(locale);
                    
                    for (int i=0; i < lines.size()-1; i++) {
                        String first=lines.get(i);
                        for (int j=i+1; j < lines.size(); j++) {
                            if (collator.compare(first, lines.get(j)) == 0) {
                                lines.remove(j);
                                j--;
                            }
                        }
                    }
                    
                    doc.remove(currEditor.getLineStartOffset(firstLine), 
                                currEditor.getLineEndOffset(lastLine)-
                                currEditor.getLineStartOffset(firstLine));
                    
                    for (int i=firstLine; i < firstLine + lines.size(); i++) {
                        int lineStart=currEditor.getLineStartOffset(i);
                        doc.insertString(lineStart, lines.get(i-firstLine), null);
                    }
                    
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                
            };
        };
    }

    /**
     * Returns exit action described in JNotepadPP documentation.
     * @return the exitAction
     */
    public Action getExitAction() {
        return new LocalizableAction("Exit", "exitDesc", notepad.getFlp()) {
            
            /**
             * Default serial version.
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                
                JTabbedPane tabs=notepad.getTabs();
                int index=tabs.getSelectedIndex();
                
                if (notepad.checkReadyToClose()) {
                    System.exit(0);
                } else {
                    tabs.setSelectedIndex(index);
                }
            }
            
        };
    }
    
    

}
