package main.torrent.protocol.requests;

import main.torrent.protocol.TorrentProtocolHelper;
import main.util.Messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class RequestRequest extends NonHandshakeRequest {

    Logger logger = Logger.getLogger(RequestRequest.class.getName());

    private int pieceIndex;
    private int begin;
    private int length;

    public RequestRequest(ByteBuffer requestBuffer) {
        super(requestBuffer);
        this.pieceIndex = requestBuffer.getInt();
        this.begin = requestBuffer.getInt();
        this.length = requestBuffer.getInt();
    }

    @Override
    public void processRequest() {
        try {
            ByteBuffer pieceBuffer = this.peer.retrieveDataBlock(pieceIndex, begin, length);
            peer.sendMessage(TorrentProtocolHelper.createPiece(this.pieceIndex, this.begin, pieceBuffer));
        } catch (IOException e) {
            logger.log(Level.INFO, Messages.REQUEST_PROCESS_FAIL.getText());
        }
    }
}
