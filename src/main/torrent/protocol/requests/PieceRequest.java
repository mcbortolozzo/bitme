package main.torrent.protocol.requests;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

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
        try {
            this.peer.writeDataBlock(pieceIndex, begin, block);
            boolean pieceDone = this.peer.verifyPieceHash(pieceIndex);
            if(pieceDone) this.peer.sendHave(pieceIndex);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
