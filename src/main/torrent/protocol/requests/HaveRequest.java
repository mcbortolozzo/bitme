package main.torrent.protocol.requests;

import java.nio.ByteBuffer;

/**
 * Created by marcelo on 19/11/16.
 */
public class HaveRequest extends NonHandshakeRequest {

    private int pieceIndex;

    public HaveRequest(ByteBuffer requestBuffer){
        super(requestBuffer);
        this.pieceIndex = requestBuffer.getInt(); //message has fixed size, no need for verifications
    }

    @Override
    public void processRequest() {
        this.peer.setHavePiece(this.pieceIndex);
    }

    public int getPieceIndex() {
        return pieceIndex;
    }
}
