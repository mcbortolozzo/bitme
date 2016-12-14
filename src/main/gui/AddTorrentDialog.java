package main.gui;

import main.torrent.TorrentManager;
import main.torrent.file.TorrentFileInfo;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class AddTorrentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JButton modifierButton;
    private JCheckBox ouvrirAprèsLaCréationCheckBox;
    private JLabel filenameLabel;
    private JLabel sizeLabel;
    private JLabel localisationLabel;

    private final JFileChooser fc = new JFileChooser();
    private File toSave;
    private File destination;

    private TorrentManager tManager;

    public AddTorrentDialog(final File f, final TorrentManager t) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.filenameLabel.setText(f.getName());
        this.sizeLabel.setText(TorrentTableModel.prettySizePrint(f.length()));
        this.localisationLabel.setText(f.getParent());

        this.toSave = f;
        this.destination = f.getParentFile();
        this.tManager = t;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        modifierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setDialogTitle("Selectionnez où enregistrer votre fichier torrent");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(AddTorrentDialog.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    AddTorrentDialog.this.destination = fc.getSelectedFile();
                    AddTorrentDialog.this.localisationLabel.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }

    private void onOK() {

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void display(File f, TorrentManager manager) {
        AddTorrentDialog dialog = new AddTorrentDialog(f, manager);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
