package main.torrent;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentManager {
    private static TorrentManager instance = null;
    private HashMap<String, TorrentFile> torrentList;

    protected TorrentManager(){
        this.torrentList = new HashMap<String, TorrentFile>();
    }

    public static TorrentManager getInstance(){
        if(instance == null){
            instance = new TorrentManager();
        }
        return instance;
    }

    public synchronized TorrentFile retrieveTorrent(String torrentId) {
        return this.torrentList.get(torrentId);
    }

    //TODO upgrade method to retrieve data from file
    public synchronized void addTorrent(String torrentId){
        TorrentFile torrentFile = new TorrentFile(torrentId);
        torrentList.put(torrentId, torrentFile);
        //TODO take action if file already in map?
    }
}
