package main.torrent.protocol.requests;

import main.peer.Bitfield;
import main.torrent.protocol.RequestTypes;

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
    private byte[] bitfieldBytes;

    public BitfieldRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
        bitfieldBytes = new byte[Math.toIntExact(this.getMessageLength() - 1)]; // length minus id = received bitfield length
        requestBuffer.get(bitfieldBytes);
    }

    @Override
    public void processRequest() {
        this.bitfield = Bitfield.generateBitset(this.bitfieldBytes, this.peer.getBitfield().getBitfieldLength());
        this.peer.updateBitfield(this.bitfield);
    }
}

