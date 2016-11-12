package main.torrent;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentFile {

    private String torrentId;
    //TODO complete constructor and class methods
    public TorrentFile(String torrentId){
        this.torrentId = torrentId;
    }

    public String getTorrentId() {
        return torrentId;
    }
}
