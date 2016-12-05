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
        byte[] bitfieldBytes = new byte[this.getMessageLength() - 1]; // length minus id = received bitfield length
        requestBuffer.get(bitfieldBytes);
        this.bitfield = BitSet.valueOf(bitfieldBytes);
    }

    @Override
    public void processRequest() {
        this.peer.updateBitfield(this.bitfield);
    }
}

