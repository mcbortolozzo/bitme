package main.gui;

import main.peer.Peer;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.util.Utils;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aval0n on 15/12/2016.
 */
public class PeersTableModel extends AbstractTableModel {

    List<Peer> peers = new ArrayList<>();

    private String[] title = {"IP", "Reçu", "Vitesse DL", "Envoyé", "Vitesse Up"};

    public PeersTableModel(TorrentManager manager) {
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                peers.sort(Utils.PeerSpeedComparator);
                PeersTableModel.this.fireTableRowsUpdated(0, peers.size() - 1);
            }
        });
        timer.start();
    }

    public void setTorrent(TorrentFile t) { this.peers = t.getPeers(); }

    @Override
    public int getRowCount() { return peers.size(); }

    @Override
    public int getColumnCount() { return title.length; }

    @Override
    public String getColumnName(int columnIndex) { return title[columnIndex]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Peer p = peers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return p.getPeerIp();
            case 1:
                return Utils.prettySizePrint(p.getDownloaded());
            case 2:
                return Utils.prettySizePrint(Utils.getSpeedFromLog(p.getUDowloadLog()));
            case 3:
                return Utils.prettySizePrint(p.getUploaded());
            case 4:
                return Utils.prettySizePrint(Utils.getSpeedFromLog(p.getUploadLog()));
            default:
                return null;
        }
    }
}
