package main.torrent.protocol.requests;

import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by marcelo on 07/11/16.
 */
public class HandshakeRequest extends TorrentRequest {
    private int pstrlen;
    private final byte[] protocol;
    private final byte[] reservedByte;
    private final HashId torrentId;
    private final HashId otherPeerId;

    public HandshakeRequest(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        byte[] messageBytes = new byte[TorrentProtocolHelper.HANDSHAKE_SIZE];
        messageBuffer.get(messageBytes);
        this.pstrlen = messageBytes[0];
        this.protocol = Arrays.copyOfRange(messageBytes, 1, 1 + pstrlen);
        this.reservedByte = Arrays.copyOfRange(messageBytes, 1 + pstrlen, 1 + pstrlen + 8);
        this.torrentId = new HashId(Arrays.copyOfRange(messageBytes, 1 + pstrlen + 8, 1 + pstrlen + 28));
        this.otherPeerId = new HashId(Arrays.copyOfRange(messageBytes, 1 + pstrlen + 28, messageBytes.length));
    }

    @Override
    public void processRequest() {
        //TODO get Torrent id
        if(this.peer.getOtherPeerId() == null) { // if we already have the other peer Id the handshake has been completed before
            TorrentManager torrentManager = TorrentManager.getInstance();
            TorrentFile torrentFile = torrentManager.retrieveTorrent(this.torrentId);
             if (torrentFile != null) {
                 peer.setLocalPeerId(torrentFile.getPeerId());
                 if(this.otherPeerId.equals(this.peer.getLocalPeerId())){ // close connections with self
                     this.peer.shutdown();
                 } else {
                     peer.setTorrentFile(torrentFile);
                     peer.setOtherPeerId(this.otherPeerId);
                     if (!peer.isHandshakeSent()){
                         peer.sendHandshake(this);
                     }

                     //TODO not send if empty
                     peer.sendBitfield(this);
                 }
            } else {
                //TODO handle torrent not found or just ignore?
            }
        }

    }

}
