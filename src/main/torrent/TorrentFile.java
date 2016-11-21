package main.torrent;

import main.peer.Peer;
import main.torrent.file.TorrentFileInfo;
import main.torrent.HashId;
import java.io.IOException;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentFile {

    private final HashId peerId;
    private HashId torrentId;

    private TorrentFileInfo fileInfo;
    private String filePath;

    private Long pieceSize;
    private int pieceCount;


    //TODO complete constructor and class methods
    public TorrentFile(HashId torrentId) throws IOException {
        this.torrentId = torrentId;
        this.peerId = Peer.generatePeerId();
        this.pieceCount = 2000; //TODO remove this

    }

    public TorrentFile(String filePath, TorrentFileInfo fileInfo) throws IOException {
        this.filePath = filePath;
        this.fileInfo = fileInfo;
        this.torrentId = new HashId(fileInfo.getInfoHash());
        this.peerId = Peer.generatePeerId();
        this.pieceCount = this.fileInfo.getPieceCount();
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
