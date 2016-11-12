package main.torrent.protocol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by marcelo on 07/11/16.
 */
public enum RequestTypes {
    HANDSHAKE, NONE;

    public static RequestTypes getMessageType(ByteBuffer messageBuffer) {
        if(isHandshake(messageBuffer))
            return HANDSHAKE;

        return NONE;
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

    public TorrentRequest generateRequest(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        switch (this){
            case HANDSHAKE:
                return new HandshakeRequest(messageBuffer);
            default:
                return null;
        }
    }
}
