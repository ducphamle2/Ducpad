/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ducpad;

import com.sun.glass.events.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Duc Pham Le
 */
public class FileChooser {

    /* init component */
    private JFrame frame = null;
    private JMenuBar menuBar = null;
    private JMenu fileMenu = null;
    private JMenu editMenu = null;
    private JMenuItem newItem = null;
    private JMenuItem openItem = null;
    private JMenuItem saveItem = null;
    private JMenuItem exitItem = null;
    private JMenuItem aboutItem = null;
    private JMenuItem copyItem = null;
    private JMenuItem pasteItem = null;
    private JMenuItem cutItem = null;
    private JMenuItem selectAllItem = null;
    private JTextArea area = null;
    private JScrollPane scrollPane = null;
    private JFileChooser chooser = null;
    private JCheckBoxMenuItem lineWrap = null;
    private JCheckBoxMenuItem wrapWord = null;
    private JPopupMenu popupMenu = null;

    /* init Listener */
    private ActionListener menuListener = null;
    private KeyListener keyListener = null;
    private MouseListener mouseListener = null;
    private DocumentListener docListener = null;

    /* init data type */
    private static final int FILE_OPEN = 1;
    private static final int FILE_SAVE = 2;
    private boolean flag = false; //flag variable is used to check if the file is saved directly or through open, new, ...
    private String stringPrev = null;
    private String stringCur = null;
    private String copiedString = null;
    private boolean[] menuItemCheck; //check if a hotkey combo is pressed correctly

    private void dataInit() {
        menuItemCheck = new boolean[256];

        frame = new JFrame();
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        popupMenu = new JPopupMenu();
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        exitItem = new JMenuItem("Exit");
        aboutItem = new JMenuItem("About");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");
        cutItem = new JMenuItem("Cut");
        selectAllItem = new JMenuItem("Select All");
        lineWrap = new JCheckBoxMenuItem("Line wrap");
        lineWrap.setState(true);
        wrapWord = new JCheckBoxMenuItem("Wrap word style");
        wrapWord.setState(true);
        area = new JTextArea();
        scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chooser = new JFileChooser();

        stringCur = "";
        stringPrev = "";
        copiedString = "";

        /* some override methods below for listener components */
        menuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuItemPerformed(e);
            }
        };

        /* this listener is used to catch events when closing the app */
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!area.getText().equals("") && !area.getText().equals(stringPrev)) {
                    int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
                    if (temp == JOptionPane.OK_OPTION) {
                        flag = true;
                        operateFile("Save a file", FILE_SAVE);
                        System.exit(0);
                    } else if (temp == JOptionPane.NO_OPTION) {
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        });

        keyListener = new KeyListener() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
            }

            @Override
            /*  because the entire keyboard is fixed to be below 128 so
                when getting the keycode, if it's not the custom keyboard then
                we will toggle the index in keyPressed array to be true.
             */
            public void keyPressed(java.awt.event.KeyEvent e) {
                stringCur = area.getText(); //if any changes is made, string cur will be changed different from string prev
                int key = e.getKeyCode();
                if (key < KeyBoard.keyPressed.length) {
                    KeyBoard.keyPressed[key] = true;
                }
                keyItemPerformed();
            }

            @Override
            //opposite to the keyPressed method
            public void keyReleased(java.awt.event.KeyEvent e) {
                int key = e.getKeyCode();
                if (key < KeyBoard.keyPressed.length) {
                    KeyBoard.keyPressed[key] = false;
                }
            }
        };

        //init mouse listener
        mouseListener = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (area.getSelectedText() != null) {
                    copiedString = area.getSelectedText();
                    copyItem.setEnabled(true);
                    cutItem.setEnabled(true);
                } else {
                    copyItem.setEnabled(false);
                    cutItem.setEnabled(false);
                }
            }
        };
        
        docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (area.getText().length() >= 1) 
                    selectAllItem.setEnabled(true);
                else
                    selectAllItem.setEnabled(false);
                String cur = area.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (area.getText().length() > 0) 
                    selectAllItem.setEnabled(true);
                else
                    selectAllItem.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (area.getText().length() >= 1) 
                    selectAllItem.setEnabled(true);
                else
                    selectAllItem.setEnabled(false);
            }
        };
    }

    /**
     * <b><i>keyItemPerformed</i></b>
     * <p>
     * private void keyItemPerformed()
     * <p>
     * This method is used to give actions when a key event occurs
     */
    private void keyItemPerformed() {
        //new file
        if (KeyBoard.isPressed(KeyEvent.VK_CONTROL) && KeyBoard.isPressed((KeyEvent.VK_N))) {
            setMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_N);
        }
        //this block of code is used to check: when a right hotkey combo is used, are there any changes in the text or not
        if (!area.getText().equals("") && !stringCur.equals(stringPrev)
                && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_N) == true) {
            //after checking, we need to reset the conditions to false.
            KeyBoard.keyInit();
            menuItemCheckerInit();
            int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
            if (temp == JOptionPane.OK_OPTION) {
                flag = true;
                operateFile("Save a file", FILE_SAVE);
                area.setText("");
                frame.setTitle("");
            } else if (temp == JOptionPane.NO_OPTION) {
                area.setText("");
                frame.setTitle("");
            }
        }
        //if the combo hotkey is correct but there are no changes in the text area
        if (stringCur.equals(stringPrev) && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_N) == true) {
            KeyBoard.keyInit();
            menuItemCheckerInit();
            area.setText("");
            frame.setTitle("");
        }

        //open file
        if (KeyBoard.isPressed(KeyEvent.VK_CONTROL) && KeyBoard.isPressed((KeyEvent.VK_O))) {
            setMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_O);
        }

        if (!area.getText().equals("") && !stringCur.equals(stringPrev)
                && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_O) == true) {
            KeyBoard.keyInit();
            menuItemCheckerInit();
            int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
            if (temp == JOptionPane.OK_OPTION) {
                flag = true;
                operateFile("Save a file", FILE_SAVE);
                operateFile("Open a file", FILE_OPEN);
            }
            if (temp == JOptionPane.NO_OPTION) {
                operateFile("Open a file", FILE_OPEN);
            }
        }

        if (stringCur.equals(stringPrev) && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_O) == true) {
            KeyBoard.keyInit();
            menuItemCheckerInit();
            operateFile("Open a file", FILE_OPEN);
        }

        //save file
        if (KeyBoard.isPressed(KeyEvent.VK_CONTROL) && KeyBoard.isPressed((KeyEvent.VK_S))) {
            operateFile("Save a file", FILE_SAVE);
            KeyBoard.keyInit();
            menuItemCheckerInit();
        }

        //exit file
        if (KeyBoard.isPressed(KeyEvent.VK_CONTROL) && KeyBoard.isPressed((KeyEvent.VK_E))) {
            setMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_E);
        }

        if (!area.getText().equals("") && !stringCur.equals(stringPrev)
                && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_E) == true) {
            KeyBoard.keyInit();
            menuItemCheckerInit();
            int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
            if (temp == JOptionPane.OK_OPTION) {
                flag = true;
                operateFile("Save a file", FILE_SAVE);
                System.exit(0);
            }
            if (temp == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }

        if (stringCur.equals(stringPrev) && getMenuItemChecked(KeyEvent.VK_CONTROL + KeyEvent.VK_E) == true) {
            System.exit(0);
        }
    }

    /**
     * <b><i>menuItemPerformed</i></b>
     * <p>
     * private void menuItemPerformed(ActionEvent e)
     * <p>
     * This method is used to give actions when a menu item event occurs
     *
     * @param ActionEvent e - event of the component that occurs
     */
    private void menuItemPerformed(ActionEvent e) {
        if (e.getSource() == newItem) {
            if (!area.getText().equals("") && !area.getText().equals(stringPrev)) {
                int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
                if (temp == JOptionPane.OK_OPTION) {
                    flag = true;
                    operateFile("Save a file", FILE_SAVE);
                    area.setText("");
                    frame.setTitle("");
                }
                if (temp == JOptionPane.NO_OPTION) {
                    area.setText("");
                    frame.setTitle("");
                }
            } else {
                area.setText("");
                frame.setTitle("");
            }
        }
        if (e.getSource() == openItem) {
            if (!area.getText().equals("") && !area.getText().equals(stringPrev)) {
                int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
                if (temp == JOptionPane.OK_OPTION) {
                    flag = true;
                    operateFile("Save a file", FILE_SAVE);
                    //after saving, we need to open the new file
                    operateFile("Open a file", FILE_OPEN);
                }
                if (temp == JOptionPane.NO_OPTION) {
                    operateFile("Open a file", FILE_OPEN);
                }
            } else {
                operateFile("Open a file", FILE_OPEN);
            }
        }
        if (e.getSource() == saveItem) {
            operateFile("Save a file", FILE_SAVE);
        }
        if (e.getSource() == exitItem) {
            if (!area.getText().equals("") && !area.getText().equals(stringPrev)) {
                int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
                if (temp == JOptionPane.OK_OPTION) {
                    flag = true;
                    operateFile("Save a file", FILE_SAVE);
                    System.exit(0);
                }
                if (temp == JOptionPane.NO_OPTION) {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        }

        //this part is for editMenu
        if (e.getSource() == lineWrap) {
            if (lineWrap.getState()) {
                area.setLineWrap(true);
            } else {
                area.setLineWrap(false);
            }
        }

        if (e.getSource() == wrapWord) {
            if (wrapWord.getState()) {
                area.setWrapStyleWord(true);
            } else {
                area.setWrapStyleWord(false);
            }
        }

        if (e.getSource() == aboutItem) {
            JOptionPane.showMessageDialog(null, String.format("Version: 1.1\nDeveloper: Pham Le Duc\n"));
        }

        //if copy item is used then we will enable the paste item
        if (e.getSource() == copyItem) {
            pasteItem.setEnabled(true);
        }

        if (e.getSource() == pasteItem) {
            area.setText(area.getText() + copiedString);
            stringCur = area.getText();
        }

        if (e.getSource() == cutItem) {
            System.out.println("Copied String: " + copiedString);
            int lastIndex = area.getText().lastIndexOf(copiedString);
            int firstIndex = area.getText().indexOf(copiedString);
            System.out.format("first: %d, last: %d\n", firstIndex, lastIndex );
            StringBuilder sb = new StringBuilder(area.getText());
            sb.delete(firstIndex, lastIndex);
            area.setText(sb.toString());
            stringCur = area.getText();
        }

        if (e.getSource() == selectAllItem) {
            area.selectAll();
        }
    }

    /**
     * <b><i>operateFile</i></b>
     * <p>
     * private void operateFile(String title, int type)
     * <p>
     * This method is used to handle open and save file parts, give actions when
     * it comes to these two choices
     *
     * @param String title - title of the dialog (eg: SAVE FILE)
     * @param int type - currently two types: FILE_OPEN (int = 1) & FILE_SAVE
     * (int = 2)
     */
    private void operateFile(String title, int type) {
        chooser.setDialogTitle(title);
        int chooserOption = -1;
        switch (type) {
            case FILE_OPEN:
                //showOpenDialog will return the integer type of choice option (ok, cancel)
                chooserOption = chooser.showOpenDialog(null);
                break;
            case FILE_SAVE:
                chooserOption = chooser.showSaveDialog(null);
                break;
            default:
                break;
        }

        if (chooserOption == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            switch (type) {
                case FILE_OPEN:
                    readFile(file);
                    frame.setTitle(file.getName());
                    stringCur = area.getText(); //constantly update these variables to check the differences between them
                    stringPrev = area.getText();
                    break;
                case FILE_SAVE:
                    if (flag == false) {
                        int temp = JOptionPane.showConfirmDialog(null, "Do you want to save this file ?");
                        if (temp == JOptionPane.YES_OPTION) {
                            saveFile(file);
                            frame.setTitle(file.getName());
                        }
                    } else {
                        saveFile(file);
                        flag = false;
                    }
                    stringCur = area.getText();
                    stringPrev = area.getText();
                    break;
                default:
                    break;
            }
        }
    }

    private void readFile(File file) {
        area.setText("");
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bReader = new BufferedReader(reader);
            String line = "";
            while ((line = bReader.readLine()) != null) {
                area.append(line + "\n"); // '\r\n' because windows notepad can only understands like that
            }
            bReader.close();
            reader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void saveFile(File file) {
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            BufferedWriter bf = new BufferedWriter(fw);
            String temp = area.getText();
            temp = temp.replaceAll("(?!\\r)\\n", "\r\n");
            bf.write(temp);
            bf.close();
            fw.close();
            readFile(file);
        } catch (IOException e) {
        }
    }

    public FileChooser() {
        dataInit();
        popupMenuDisabled();
        //build frame
        frame.setBounds(700, 300, 600, 400);
        //frame.setResizable(false);
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(aboutItem);
        fileMenu.add(exitItem);
        editMenu.add(lineWrap);
        editMenu.add(wrapWord);
        popupMenu.add(cutItem);
        popupMenu.add(copyItem);
        popupMenu.add(pasteItem);
        popupMenu.addSeparator();
        popupMenu.add(selectAllItem);
        newItem.addActionListener(menuListener);
        newItem.setToolTipText("CTRL + N");
        openItem.addActionListener(menuListener);
        openItem.setToolTipText("CTRL + O");
        saveItem.addActionListener(menuListener);
        saveItem.setToolTipText("CTRL + S");
        exitItem.addActionListener(menuListener);
        exitItem.setToolTipText("CTRL + E");
        aboutItem.addActionListener(menuListener);
        copyItem.addActionListener(menuListener);
        copyItem.setToolTipText("CTRL + C");
        pasteItem.addActionListener(menuListener);
        pasteItem.setToolTipText("CTRL + V");
        cutItem.addActionListener(menuListener);
        cutItem.setToolTipText("CTRL + X");
        selectAllItem.addActionListener(menuListener);
        selectAllItem.setToolTipText("CTRL + A");
        lineWrap.addActionListener(menuListener);
        wrapWord.addActionListener(menuListener);
        area.setFocusable(true); //a component musts use this method in order to receive key events
        KeyBoard.keyInit();

        area.setBounds(0, 0, frame.getWidth(), frame.getHeight());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFocusable(true);
        area.setComponentPopupMenu(popupMenu);
        area.addKeyListener(keyListener);
        area.addMouseListener(mouseListener);
        area.getDocument().addDocumentListener(docListener);

        frame.setJMenuBar(menuBar);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    private void menuItemCheckerInit() {
        for (int i = 0; i < menuItemCheck.length; i++) {
            menuItemCheck[i] = false;
        }
    }

    private boolean getMenuItemChecked(int type) {
        return menuItemCheck[type];
    }

    private void setMenuItemChecked(int type) {
        menuItemCheck[type] = true;
    }

    private void popupMenuDisabled() {
        copyItem.setEnabled(false);
        pasteItem.setEnabled(false);
        cutItem.setEnabled(false);
        selectAllItem.setEnabled(false);
    }
}
