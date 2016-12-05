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
    private final byte[] torrentId;
    private final byte[] otherPeerId;

    public HandshakeRequest(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        byte[] messageBytes = new byte[TorrentProtocolHelper.HANDSHAKE_SIZE];
        messageBuffer.get(messageBytes);
        this.pstrlen = messageBytes[0];
        this.protocol = Arrays.copyOfRange(messageBytes, 1, 1 + pstrlen);
        this.reservedByte = Arrays.copyOfRange(messageBytes, 1 + pstrlen, 1 + pstrlen + 8);
        this.torrentId = Arrays.copyOfRange(messageBytes, 1 + pstrlen + 8, 1 + pstrlen + 28);
        this.otherPeerId = Arrays.copyOf(messageBytes, 1 + pstrlen + 28);
    }

    @Override
    public void processRequest() {
        //TODO get Torrent id
        if(this.peer.getOtherPeerId() == null) { // if we already have the other peer Id the handshake has been completed before
            TorrentManager torrentManager = TorrentManager.getInstance();
            TorrentFile torrentFile = torrentManager.retrieveTorrent(new HashId(this.torrentId));
            if (torrentFile != null) {
                peer.setTorrentFile(torrentFile);
                peer.setOtherPeerId(new HashId(this.otherPeerId));
                peer.setLocalPeerId(torrentFile.getPeerId());
                if(!peer.isHandshakeSent()) peer.sendHandshake();

                //TODO not send if empty
                peer.sendBitfield();
            } else {
                //TODO handle torrent not found or just ignore?
            }
        }

    }

}
