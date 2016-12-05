package main.torrent.protocol.requests;

import main.torrent.protocol.TorrentRequest;

import java.nio.ByteBuffer;

/**
 * Created by marcelo on 19/11/16.
 */
public abstract class NonHandshakeRequest extends TorrentRequest {

    protected int messageLength;
    protected int messageType;

    protected NonHandshakeRequest(ByteBuffer requestBuffer){
        this.messageLength = requestBuffer.getInt();
        if(this.messageLength != 0) {
            this.messageType = requestBuffer.get();
        }
    }

    public int getMessageLength() {
        return messageLength;
    }

    public int getMessageType() {
        return messageType;
    }
}
