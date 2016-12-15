package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import main.torrent.file.MultipleFileInfo;
import main.torrent.file.SingleFileInfo;
import main.torrent.file.TorrentFileInfo;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.tracker.TrackerHelper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        torrentFile.scheduleTrackerUpdate(0l, TimeUnit.SECONDS, TrackerHelper.Event.STARTED);
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


}
