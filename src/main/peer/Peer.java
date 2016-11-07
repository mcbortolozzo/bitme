package main.peer;

import main.reactor.PeerHandler;
import main.torrent.TorrentFile;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;

import java.nio.channels.SelectionKey;

/**
 * Created by marcelo on 07/11/16.
 */
public class Peer {

    private TorrentFile torrentFile;
    private SelectionKey selectionKey;
    private String peerId;


    public void process(String message){
        TorrentRequest request = TorrentProtocolHelper.decodeMessage(message);
        request.processRequest(this);
    }

    public void sendMessage(String message){
        Object object = selectionKey.attachment();
        if(object instanceof PeerHandler){
            PeerHandler peerHandler = (PeerHandler) object;
            peerHandler.addToBuffer(message);
        } else {
          //TODO treta
        }
    }

    public void setTorrentFile(TorrentFile torrentFile) {
        this.torrentFile = torrentFile;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
