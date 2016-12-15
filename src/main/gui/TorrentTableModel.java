package main.gui;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.util.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentTableModel extends AbstractTableModel {

    Logger logger = Logger.getLogger(this.getClass().getName());

    private Client c;

    private TorrentManager tManager;
    private List<TorrentFile> torrents;

    private String[] title = {"Nom", "Taille", "Progression", "Reçu", "Vitesse DL", "Envoyé", "Vitesse Up", "Ratio", "Temps Restant"};

    public TorrentTableModel(TorrentManager manager, Client c) {
        this.tManager = manager;
        this.torrents = new ArrayList<>();
        for (HashId id : this.tManager.getTorrentList().keySet())
            torrents.add(this.tManager.getTorrentList().get(id));
        this. c = c;

        Timer timer = new Timer(0, e -> {
            if (tManager.getTorrentList().size() > 0)
                TorrentTableModel.this.fireTableRowsUpdated(0, tManager.getTorrentList().size() - 1);
        });

        timer.setDelay(1000); // delay for 30 seconds
        timer.start();
    }

    private void updateList() {
        this.torrents = new ArrayList<>();
        for (HashId id : this.tManager.getTorrentList().keySet())
            torrents.add(this.tManager.getTorrentList().get(id));
    }

    @Override
    public int getRowCount() {
        return this.tManager.getTorrentList().size();
    }

    @Override
    public int getColumnCount() {
        return this.title.length;
    }

    public String getColumnName(int col) {
        return this.title[col];
    }

    public void removeTorrent(int[] is) {
        for(int i = is.length - 1; i >= 0; i--) {
            torrents.get(is[i]).shutdown();
        }
    }

    public void pauseTorrent(int[] is) {
        for (int index : is) {
            this.torrents.get(index).pause();
        }
    }

    public void startTorrent(int[] is) {
        for (int index : is) {
            this.torrents.get(index).start();
        }
    }

    public void addTorrent(final String path, final String saveFolder) {
        System.out.println("Parent : " + Thread.currentThread().getName());
        EventQueue.invokeLater(() -> {
            System.out.println("Enfant : " + Thread.currentThread().getName());
            try {
                TorrentTableModel.this.tManager.addTorrent(path, saveFolder, c.getSelector());
            } catch (IOException | BencodeReadException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
    }

    public TorrentFile getTorrentAt(int rowIndex) {
        torrents.clear();
        for (HashId id : this.tManager.getTorrentList().keySet()) {
            torrents.add(this.tManager.getTorrentList().get(id));
        }
        return this.torrents.get(rowIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        torrents.clear();
        for (HashId id : this.tManager.getTorrentList().keySet()) {
            torrents.add(this.tManager.getTorrentList().get(id));
        }
        TorrentFile current  = this.torrents.get(rowIndex);
        List<Peer> peers = current.getPeers();
        int speed;
        switch (columnIndex) {
            case 0: // Name
                return current.getFileInfo().getName();
            case 1: // Taille
                return Utils.prettySizePrint(current.getFileInfo().getLength());
            case 2: // Progression
                return 100 * ((float)current.getBitfield().getBitfield().cardinality()/current.getPieceCount());
            case 3: // Reçu
                return Utils.prettySizePrint(current.getDownloaded());
            case 4: // Vitesse Reception
                speed = 0;
                for (Peer p : peers)
                    speed += Utils.getSpeedFromLog(p.getUDowloadLog());
                return Utils.prettySizePrint(speed) + "/s";
            case 5: // Uploadé
                return Utils.prettySizePrint(current.getUploaded());
            case 6: // Vitesse Upload
                speed = 0;
                for (Peer p : peers)
                    speed += Utils.getSpeedFromLog(p.getUploadLog());
                return Utils.prettySizePrint(speed) + "/s";
            case 7: // Ratio
                return current.getDownloaded() == 0 ? 0 : new DecimalFormat("#.###").format(((float) current.getUploaded() / current.getDownloaded()));
            case 8: // Temps restant
                return "tmps restant " + rowIndex;
            default:
                return null;
        }
    }

}
