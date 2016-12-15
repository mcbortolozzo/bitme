package main.torrent.protocol.requests;

import java.nio.ByteBuffer;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class CancelRequest extends NonHandshakeRequest {

    private int pieceIndex;
    private int begin;
    private int length;

    public CancelRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
        this.pieceIndex = requestBuffer.getInt();
        this.begin = requestBuffer.getInt();
        this.length = requestBuffer.getInt();
    }

    @Override
    public void processRequest() {
        //throw new UnsupportedOperationException();
    }
}
