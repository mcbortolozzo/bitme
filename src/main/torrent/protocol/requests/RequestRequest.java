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

    public int getPieceIndex() {
        return pieceIndex;
    }

    public int getBegin() {
        return begin;
    }

    @Override
    public void processRequest() {
        try {
            ByteBuffer pieceBuffer = this.peer.retrieveDataBlock(pieceIndex, begin, length);
            this.peer.addUploaded(this.length);
            peer.sendReactiveMessage(TorrentProtocolHelper.createPiece(this.pieceIndex, this.begin, pieceBuffer), this);
        } catch (IOException e) {
            logger.log(Level.INFO, Messages.REQUEST_PROCESS_FAIL.getText());
        }
    }
}
