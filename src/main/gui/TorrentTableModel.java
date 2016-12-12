package main.gui;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aval0n on 28/11/2016.
 */
public class TorrentTableModel extends AbstractTableModel {

    private Client c;

    private TorrentManager tManager;
    private List<TorrentFile> torrents;

    private String[] title = {"Nom", "Taille", "Progression", "Reçu", "Vitesse DL", "Envoyé", "Vitesse Up", "Ratio", "Temps Restant"};

    public TorrentTableModel(TorrentManager manager, Client c) {
        this.tManager = manager;
        this.torrents = new ArrayList<>();
        for (HashId id : this.tManager.getTorrentList().keySet())
            torrents.add(this.tManager.getTorrentList().get(id));
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
        // TODO Implement torrent removal
        // Should remove the torrent from the manager
        // And then refresh torrents
        System.out.println("Not Implmented Yet, thanks to the Network team #Brazil");

    }

    public void addTorrent(String path, String saveFolder) {
        try {
            System.out.println(new File(path).getParent());
            this.tManager.addTorrent(path, new File(path).getParent(), c.getSelector());
            this.fireTableDataChanged();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BencodeReadException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        for (HashId id : this.tManager.getTorrentList().keySet()) {
            System.out.println("BOUMMMMMMMM");
            torrents.add(this.tManager.getTorrentList().get(id));
        }
        TorrentFile current = this.torrents.get(rowIndex);
        switch (columnIndex) {
            case 0: // Name
                return current.getFileInfo().getName();
            case 1: // Taille
                return prettySizePrint(current.getFileInfo().getLength());
            case 2: // Progression
                return prettySizePrint(current.getDownloaded());
            case 3: // Reçu
                return current.getDownloaded();
            case 4: // Vitesse Reception
                return "dl speed " + rowIndex;
            case 5: // Uploadé
                return prettySizePrint(current.getUploaded());
            case 6: // Vitesse Upload
                return "up speed " + rowIndex;
            case 7: // Ratio
                return current.getDownloaded() == 0 ? 0 : current.getUploaded() / current.getDownloaded();
            case 8: // Temps restant
                return "tmps restant " + rowIndex;
            default:
                return null;
        }
    }

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    protected String prettySizePrint(long value) {
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if (value == 0)
            return "0 kb";
        if (value < 0)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(value >= divider){
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    protected static String format(long value, long divider, String unit){
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", Double.valueOf(result), unit);
    }
}
