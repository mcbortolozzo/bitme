package main.torrent.protocol;

import main.peer.Peer;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;

/**
 * Created by marcelo on 07/11/16.
 */
public class HandshakeRequest extends TorrentRequest {

    @Override
    public void processRequest(Peer peer) {
        //TODO get Torrent id
        String torrentId = null;
        TorrentManager torrentManager = TorrentManager.getInstance();
        TorrentFile torrentFile = torrentManager.retrieveTorrent(torrentId);
        if(torrentFile != null){
            String thisPeerId = null;   //TODO get this peer id (not object peer, which is the other one)
            String otherPeerId = null;
            peer.setTorrentFile(torrentFile);
            peer.setPeerId(otherPeerId);
            String message = TorrentProtocolHelper.createHandshake(torrentId, thisPeerId);
            peer.sendMessage(message);
        } else {
            //TODO handle torrent not found
        }

    }

}
