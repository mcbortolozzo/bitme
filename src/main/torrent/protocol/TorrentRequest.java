package main.torrent.protocol;

import main.peer.Peer;

/**
 * Created by marcelo on 07/11/16.
 */
public abstract class TorrentRequest implements TorrentRequestInterface, Runnable {

    protected Peer peer;

    @Override
    public void run() {
        this.processRequest();
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
