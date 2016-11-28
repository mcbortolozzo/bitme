package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;
import main.torrent.file.MultipleFileInfo;
import main.torrent.file.SingleFileInfo;
import main.torrent.file.TorrentFileInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentManager {
    private static TorrentManager instance = null;
    private HashMap<HashId, TorrentFile> torrentList;

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

    //TODO upgrade method to retrieve data from file
    public synchronized void addTorrent(HashId torrentId, int pieceCount) throws IOException {
        TorrentFile torrentFile = new TorrentFile(torrentId);
        torrentList.put(torrentId, torrentFile);
        //TODO take action if file already in map?
    }

    public void addTorrent(String filePath) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFileInfo fileInfo = readFileInfo(filePath);
        TorrentFile torrentFile = new TorrentFile(filePath, fileInfo);
        synchronized (this){
            torrentList.put(torrentFile.getTorrentId(), torrentFile);
        }
    }

    private TorrentFileInfo readFileInfo(String filePath) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        Map<String,Object> dict = null;
        Map<String,Object> info;
        InputStream in = new FileInputStream(filePath);

        BencodeReader benReader = new BencodeReader(in);
        dict = benReader.readDict();

        info = (Map<String, Object>) dict.get("info");
        if(dict.containsKey("files")){
            return new MultipleFileInfo(dict);
        } else {
            return new SingleFileInfo(dict);
        }
    }


}
