package main.torrent.protocol;

import main.torrent.protocol.requests.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by marcelo on 07/11/16.
 */
public enum RequestTypes {
    NONE(-3),KEEP_ALIVE(-2), HANDSHAKE(-1), CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3), HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), CANCEL(8);

    private final int id;

    RequestTypes(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private static RequestTypes getById(int id){
        for(RequestTypes r : values()){
            if(r.id == id) return r;
        }
        return NONE;
    }

    public static RequestTypes getMessageType(ByteBuffer messageBuffer) {
        if(isHandshake(messageBuffer))
            return HANDSHAKE;
        else if(messageBuffer.remaining() > 4)
            return getOtherRequestType(messageBuffer);
        else
            return RequestTypes.NONE;
    }

    static boolean isHandshake(ByteBuffer messageBuffer){
        byte pstrlen = messageBuffer.get();
        boolean result = false;
        if(pstrlen == TorrentProtocolHelper.PSTRLEN){
            byte[] protocol = new byte[pstrlen];
            messageBuffer.get(protocol);
            //validate same protocol name and remaining enough for message
            result = Arrays.equals(protocol, TorrentProtocolHelper.PROTOCOL_VERSION.getBytes())
                        && messageBuffer.remaining() >= TorrentProtocolHelper.HANDSHAKE_SIZE - pstrlen - 1;
        }
        messageBuffer.rewind();
        return result;
    }

    private static RequestTypes getOtherRequestType(ByteBuffer messageBuffer){
        messageBuffer.getInt(); //ignore message length for this step
        byte id = messageBuffer.get();
        messageBuffer.rewind();
        return getById(id);
    }

    public TorrentRequest generateRequest(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        switch (this){
            case KEEP_ALIVE:
                return new KeepAliveRequest(messageBuffer);
            case HANDSHAKE:
                return new HandshakeRequest(messageBuffer);
            case CHOKE:
            case UNCHOKE:
            case INTERESTED:
            case NOT_INTERESTED:
                return new StateChangeRequest(messageBuffer, this);
            case HAVE:
                return new HaveRequest(messageBuffer);
            case BITFIELD:
                return new BitfieldRequest(messageBuffer);
            case REQUEST:
                return new RequestRequest(messageBuffer);
            case PIECE:
                return new PieceRequest(messageBuffer);
            case CANCEL:
                return new CancelRequest(messageBuffer);
            default:
                return null;
        }
    }

    public static boolean isStateChange(RequestTypes stateRequest) {
        return stateRequest.equals(CHOKE) || stateRequest.equals(UNCHOKE)
                || stateRequest.equals(INTERESTED) || stateRequest.equals(NOT_INTERESTED);
    }
}
