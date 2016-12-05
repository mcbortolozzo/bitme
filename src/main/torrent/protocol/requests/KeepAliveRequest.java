package main.torrent.protocol.requests;

import java.nio.ByteBuffer;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class KeepAliveRequest extends NonHandshakeRequest {

    public KeepAliveRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
    }

    @Override
    public void processRequest() {
    }
}
