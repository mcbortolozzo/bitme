package main.torrent.protocol;

import main.torrent.protocol.requests.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by marcelo on 07/11/16.
 */
public enum RequestTypes {
    NONE(-3, 0, true),KEEP_ALIVE(-2, 0, true), HANDSHAKE(-1, 0, true), CHOKE(0, 1, true), UNCHOKE(1, 1, true),
    INTERESTED(2, 1, true), NOT_INTERESTED(3, 1, true), HAVE(4, 5, true), BITFIELD(5, 1, false), REQUEST(6, 13, true),
    PIECE(7, 9, false), CANCEL(8, 13, true);

    private final int id;
    private final int minLength;
    private final boolean fixedLength;

    RequestTypes(int id, int minLength, boolean fixedLength) {
        this.id = id;
        this.minLength = minLength;
        this.fixedLength = fixedLength;
    }

    public int getId() {
        return id;
    }

    private static RequestTypes getById(int id, int messageLength){
        for(RequestTypes r : values()){
            if(r.id == id){
                if((r.fixedLength && messageLength == r.minLength)
                        || (!r.fixedLength && messageLength >= r.minLength)){
                    return r;
                }
            }
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
        int messageLength = messageBuffer.getInt();
        byte id = messageBuffer.get();
        messageBuffer.rewind();
        if(messageBuffer.remaining() >= messageLength && messageLength > 0)
            return getById(id, messageLength);
        else
            return NONE;
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
