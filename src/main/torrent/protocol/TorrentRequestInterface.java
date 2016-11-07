package main.torrent.protocol;

import main.peer.Peer;

/**
 * Created by marcelo on 07/11/16.
 */
public interface TorrentRequestInterface {

    public void processRequest(Peer peer);
}
