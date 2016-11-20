package main.torrent.protocol;

import main.peer.Peer;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by marcelo on 07/11/16.
 */
public class HandshakeRequest extends TorrentRequest {
    private int pstrlen;
    private final String protocol;
    private final String reservedByte;
    private final String torrentId;
    private final String otherPeerId;

    public HandshakeRequest(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        byte[] bytes = new byte[TorrentProtocolHelper.HANDSHAKE_SIZE];
        messageBuffer.get(bytes);
        String message = new String(bytes, "UTF-8");
        this.pstrlen = message.charAt(0);
        this.protocol = message.substring(1, 1 + pstrlen);
        this.reservedByte = message.substring(1 + pstrlen, 1 + pstrlen + 8);
        this.torrentId = message.substring(1 + pstrlen + 8, 1 + pstrlen + 28);
        this.otherPeerId = message.substring(1 + pstrlen + 28);
    }

    @Override
    public void processRequest() {
        //TODO get Torrent id
        if(this.peer.getOtherPeerId() == null) { // if we already have the other peer Id the handshake has been completed before
            TorrentManager torrentManager = TorrentManager.getInstance();
            TorrentFile torrentFile = torrentManager.retrieveTorrent(this.torrentId);
            if (torrentFile != null) {
                peer.setTorrentFile(torrentFile);
                peer.setOtherPeerId(this.otherPeerId);
                ByteBuffer message = TorrentProtocolHelper.createHandshake(this.torrentId, peer.getLocalPeerId());
                peer.sendMessage(message);
            } else {
                //TODO handle torrent not found or just ignore?
            }
        }

    }

}
