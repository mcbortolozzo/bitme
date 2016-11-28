package main.torrent.protocol.requests;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class BitfieldRequest extends NonHandshakeRequest {

    private BitSet bitfield;

    public BitfieldRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
        this.bitfield = BitSet.valueOf(requestBuffer);
    }

    @Override
    public void processRequest() {
        this.peer.updateBitfield(this.bitfield);
    }
}

