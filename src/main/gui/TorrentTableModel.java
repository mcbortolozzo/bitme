package main.gui;

import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aval0n on 28/11/2016.
 */
public class TorrentTableModel extends AbstractTableModel {

    private TorrentManager tManager;
    private List<TorrentFile> torrents;

    private String[] title = {"Nom", "Taille", "Progression", "Reçu", "Vitesse DL", "Envoyé", "Vitesse Up", "Ratio", "Temps Restant"};

    public TorrentTableModel(TorrentManager manager) {
        this.tManager = manager;
        List<TorrentFile> torrents = new ArrayList<>();
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
        // TODO Implement torrent removal
        // Should remove the torrent from the manager
        // And then refresh torrents
        System.out.println("Not Implmented Yet, thanks to the Network team #Brazil");

    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TorrentFile current = this.torrents.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return "Un fichier torrent " + rowIndex;
            case 1:
                return "Super Size " + rowIndex;
            case 2:
                return 50;
            case 3:
                return current.getDownloaded();
            case 4:
                return "dl speed " + rowIndex;
            case 5:
                return current.getUploaded();
            case 6:
                return "up speed " + rowIndex;
            case 7:
                return current.getDownloaded() == 0 ? 0 : current.getUploaded() / current.getDownloaded();
            case 8:
                return "tmps restant " + rowIndex;
            default:
                return null;
        }
    }
}
