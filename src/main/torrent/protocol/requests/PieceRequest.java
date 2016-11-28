package main.torrent.protocol.requests;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.ByteBuffer;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class PieceRequest extends NonHandshakeRequest {

    private int pieceIndex;
    private int begin;
    private byte[] block;

    public PieceRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
        this.pieceIndex = requestBuffer.getInt();
        this.begin = requestBuffer.getInt();
        requestBuffer.get(block);
    }

    @Override
    public void processRequest() {
        throw new UnsupportedOperationException();
    }
}
