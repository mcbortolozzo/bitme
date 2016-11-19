package main.torrent;

import java.io.IOException;
import java.util.HashMap;

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

    public synchronized TorrentFile retrieveTorrent(HashId torrentId) {
        return this.torrentList.get(torrentId);
    }

    //TODO upgrade method to retrieve data from file
    public synchronized void addTorrent(HashId torrentId, int pieceSize, int pieceCount) throws IOException {
        TorrentFile torrentFile = new TorrentFile(torrentId, pieceSize, pieceCount);
        torrentList.put(torrentId, torrentFile);
        //TODO take action if file already in map?
    }
}
