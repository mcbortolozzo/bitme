package main.torrent;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentFile {

    private String torrentId;

    private int pieceSize;
    private int pieceCount;
    //TODO complete constructor and class methods
    public TorrentFile(String torrentId, int pieceSize, int pieceCount){
        this.torrentId = torrentId;
        this.pieceSize = pieceSize;
        this.pieceCount = pieceCount;
    }

    public String getTorrentId() {
        return torrentId;
    }

    public int getPieceCount() { return this.pieceCount;
    }
}
