package main.torrent;

import main.peer.Peer;

import java.io.IOException;

/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentFile {

    private final HashId peerId;
    private HashId torrentId;

    private int pieceSize;
    private int pieceCount;
    //TODO complete constructor and class methods
    public TorrentFile(HashId torrentId, int pieceSize, int pieceCount) throws IOException {
        this.torrentId = torrentId;
        this.pieceSize = pieceSize;
        this.pieceCount = pieceCount;
        this.peerId = Peer.generatePeerId();
    }

    public HashId getTorrentId() {
        return torrentId;
    }

    public int getPieceCount() { return this.pieceCount;
    }

    public HashId getPeerId() {
        return peerId;
    }

    //TODO get uploaded, downloaded and left according to peers
    public int getUploaded() {
        return 0;
    }

    public int getDownloaded(){
        return 0;
    }

    public int getLeft(){
        return 0;
    }
}
