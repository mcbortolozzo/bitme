package main.torrent;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentManager {
    private static TorrentManager instance = null;

    protected TorrentManager(){

    }

    public static TorrentManager getInstance(){
        if(instance == null){
            instance = new TorrentManager();
        }
        return instance;
    }

    public TorrentFile retrieveTorrent(String torrentId) {
        return null;// TODO create torrent lists and retrive and synch
    }
}
