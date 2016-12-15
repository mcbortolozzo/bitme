package main.gui;

import main.torrent.TorrentManager;
import main.torrent.file.TorrentFileInfo;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;


/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class AddTorrentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea trackers;
    private JTextArea comments;
    private JButton modifierButton;
    private JCheckBox ouvrirAprèsLaCréationCheckBox;
    private JLabel filenameLabel;
    private JLabel sizeLabel;
    private JLabel localisationLabel;
    private JTextField tailleDesPiècesTextField;
    private JFormattedTextField pieceSize;

    private final JFileChooser fc = new JFileChooser();
    private File toSave;
    private File destination;

    private TorrentManager tManager;

    public AddTorrentDialog(final File f, final TorrentManager t) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.filenameLabel.setText(f.getName() + ".torrent");
        this.sizeLabel.setText(TorrentTableModel.prettySizePrint(f.length()));
        this.localisationLabel.setText(f.getParent());

        this.toSave = f;
        this.destination = f.getParentFile();
        this.tManager = t;
        this.tailleDesPiècesTextField.setText(256 + "");

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        //this.pieceSize.setFormatterFactory(new);

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
        String trackers = this.trackers.getText();
        String comments = this.comments.getText();
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
