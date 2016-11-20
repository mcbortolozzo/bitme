package main.torrent.protocol.requests;

import main.torrent.protocol.RequestTypes;

import java.nio.ByteBuffer;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class StateChangeRequest extends NonHandshakeRequest {

    RequestTypes type;

    public StateChangeRequest(ByteBuffer requestBuffer, RequestTypes type) {
        super(requestBuffer);
        this.type = type;
    }

    @Override
    public void processRequest() {
        this.peer.setState(type);
    }

}
