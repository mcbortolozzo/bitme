package main.gui;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.torrent.TorrentManager;
import main.torrent.file.TorrentFileInfo;
import main.util.Utils;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
    private JFormattedTextField pieceSize;
    private JTextField destinationName;

    private final JFileChooser fc = new JFileChooser();
    private File toSave;
    private File destination;

    private Client c;

    private TorrentManager tManager;

    public AddTorrentDialog(Client c, final File f, final TorrentManager t) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.sizeLabel.setText(Utils.prettySizePrint(f.length()));
        this.localisationLabel.setText(f.getParent());

        this.toSave = f;
        this.destination = f.getParentFile();
        this.tManager = t;

        this.destinationName.setText(this.toSave.getName());

        /*NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        this.pieceSize.
        this.pieceSize.setFormatterFactory(new);*/

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        modifierButton.addActionListener(e -> {
            fc.setDialogTitle("Selectionnez où enregistrer votre fichier torrent");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(AddTorrentDialog.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                AddTorrentDialog.this.destination = fc.getSelectedFile();
                AddTorrentDialog.this.localisationLabel.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
    }

    private void onOK() {
        if (this.trackers.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Veuillez ajouter au moins un tracker");
            return;
        }

        String[] trackers = this.trackers.getText().split("\n");
        String comments = this.comments.getText();
        try {
            int psize = Integer.parseInt(pieceSize.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "La taille des pièces doit être un entier.");
            return;
        }
        //TorrentManager.getInstance().createTorrent(destination, destinationName.getText() + ".torrent", toSave, trackers, comments, psize);
        if (ouvrirAprèsLaCréationCheckBox.isSelected()) {
            try {
                TorrentManager.getInstance().addTorrent(toSave.getName() + ".torrent", toSave.getParent(), c.getSelector());
            } catch (BencodeReadException | NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void display(Client c, File f, TorrentManager manager) {
        AddTorrentDialog dialog = new AddTorrentDialog(c, f, manager);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
