package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;
import main.torrent.file.MultipleFileInfo;
import main.torrent.file.SingleFileInfo;
import main.torrent.file.TorrentFileInfo;

import java.io.*;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentManager {
    private static TorrentManager instance = null;
    private HashMap<HashId, TorrentFile> torrentList;
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);


    protected TorrentManager(){
        this.torrentList = new HashMap<HashId, TorrentFile>();
    }

    public static TorrentManager getInstance(){
        if(instance == null){
            instance = new TorrentManager();
        }
        return instance;
    }

    public synchronized void removeTorrent(HashId id) {
        torrentList.remove(id);
    }

    public synchronized  HashMap<HashId, TorrentFile> getTorrentList() { return this.torrentList; }

    public synchronized TorrentFile retrieveTorrent(HashId torrentId) {
        return this.torrentList.get(torrentId);
    }

    public TorrentFile addTorrent(String filePath, String saveFileFolder, Selector selector) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFileInfo fileInfo = readFileInfo(filePath, saveFileFolder);
        TorrentFile torrentFile = new TorrentFile(filePath, fileInfo, selector);
        synchronized (this){
            torrentList.put(torrentFile.getTorrentId(), torrentFile);
        }
        torrentFile.init();
        return torrentFile;
    }

    private TorrentFileInfo readFileInfo(String filePath, String saveFileFolder) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        Map<String,Object> dict = null;
        InputStream in = new FileInputStream(filePath);

        BencodeReader benReader = new BencodeReader(in, StandardCharsets.ISO_8859_1);
        dict = benReader.readDict();

        Map<String, Object> info = (Map<String, Object>) dict.get("info");
        if(info.containsKey("files")){
            return new MultipleFileInfo(dict, saveFileFolder);
        } else {
            return new SingleFileInfo(dict, saveFileFolder);
        }
    }

    /**
     * Creates the .torrent file
     * @param destination
     * @param source
     * @param announce
     * @param comment
     * @param piece_length
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void createTorrent(File destination, String nameTorrent, File source, String[] announce , String comment , int piece_length) throws IOException, NoSuchAlgorithmException {

        TorrentFileInfo fileInfo = null;
        if(source.isFile()){
             fileInfo = new SingleFileInfo(destination.getAbsolutePath(), source.getName()).generateTorrent(source, source.getName(), announce, comment, piece_length);
        }
        else {
            if (source.isDirectory()) {
                fileInfo = new MultipleFileInfo(destination.getAbsolutePath(), source.getName()).generateTorrent(source,source.getName(), announce, comment, piece_length);
            }
        }

        fileInfo.bencodedFile(destination + "/" + nameTorrent);
    }


}
