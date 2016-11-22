package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import main.torrent.file.MultipleFileInfo;
import main.torrent.file.SingleFileInfo;
import main.torrent.file.TorrentFileInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public synchronized TorrentFile retrieveTorrent(HashId torrentId) {
        return this.torrentList.get(torrentId);
    }

    //TODO upgrade method to retrieve data from file
    public synchronized void addTorrent(HashId torrentId, int pieceCount) throws IOException {
        TorrentFile torrentFile = new TorrentFile(torrentId);
        torrentList.put(torrentId, torrentFile);
        //TODO take action if file already in map?
    }

    public TorrentFile addTorrent(String filePath) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFileInfo fileInfo = readFileInfo(filePath);
        TorrentFile torrentFile = new TorrentFile(filePath, fileInfo);
        synchronized (this){
            torrentList.put(torrentFile.getTorrentId(), torrentFile);
        }
        return torrentFile;
    }

    private TorrentFileInfo readFileInfo(String filePath) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        Map<String,Object> dict = null;
        InputStream in = new FileInputStream(filePath);

        BencodeReader benReader = new BencodeReader(in, StandardCharsets.ISO_8859_1);
        dict = benReader.readDict();

        if(dict.containsKey("files")){
            return new MultipleFileInfo(dict);
        } else {
            return new SingleFileInfo(dict);
        }
    }


}
