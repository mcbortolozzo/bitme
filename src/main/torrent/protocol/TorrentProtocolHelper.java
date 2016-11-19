package main.torrent.protocol;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class providing the decoding of messages, redirecting them to the request classes
 * and the generation of the messages according to the protocol
 */
public class TorrentProtocolHelper {

    public static final String PROTOCOL_VERSION = "BitTorrent protocol";
    static final char PSTRLEN = (char) PROTOCOL_VERSION.length();
    private static final int ID_LEN = 20;
    public static final int HANDSHAKE_SIZE = 1+19+8+ID_LEN*2; // pstrlen + protocol + reserved + torrent info hash + peer id
    public static final int STATE_CHANGE_LENGTH = 1; // just the id
    public static final int HAVE_LENGTH = 5; // id + piece index
    public static final int MESSAGE_LENGTH_FIELD_SIZE = 4; //length field on messages (except handshake)

    /**
     * decode the messages in a buffer, which might contain more than one in sequence
     * @param messageBuffer the buffer which contains the data received
     * @return a list of the requests decoded from the buffer
     * @throws UnsupportedEncodingException failed to put the content of the buffer in a certain encoding
     */
    public static List<TorrentRequest> decodeStream(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        List<TorrentRequest> requests = new LinkedList<>();
        while(messageBuffer.hasRemaining()){
            TorrentRequest nextReq = decodeMessage(messageBuffer);
            messageBuffer.compact();
            messageBuffer.flip();
            if(nextReq != null) {
                requests.add(nextReq);
            } else {
                return requests;
            }
        }
        return requests;
    }

    private static TorrentRequest decodeMessage(ByteBuffer messageBuffer) throws UnsupportedEncodingException {
        RequestTypes nextType = RequestTypes.getMessageType(messageBuffer);
        return nextType.generateRequest(messageBuffer);
    }

    /**
     * Generates the handshake message according to torrent and peer IDs provided
     * @param torrentId 20 byte info_hash of torrent
     * @param peerId 20 byte peer Id
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createHandshake(String torrentId, String peerId) {
        if(torrentId.length() != ID_LEN)
            throw new InvalidParameterException("Invalid torrent Id");
        if(peerId.length() != ID_LEN)
            throw new InvalidParameterException("Invalid peer Id");
        ByteBuffer handshake = ByteBuffer.allocate(HANDSHAKE_SIZE);
        handshake.put((byte) PSTRLEN);
        handshake.put(PROTOCOL_VERSION.getBytes());
        handshake.put("00000000".getBytes());   //reserved byte
        handshake.put(torrentId.getBytes());
        handshake.put(peerId.getBytes());
        return handshake;
    }

    public static ByteBuffer createStateChangeMessage(RequestTypes stateRequest){
        if(!RequestTypes.isStateChange(stateRequest))
            throw new InvalidParameterException("invalid type of request");
        return generateRequestBuffer(STATE_CHANGE_LENGTH, stateRequest.getId());
    }

    /**
     * Generic buffer header generation for non-handshake messages
     * @param messageLength the length of the message (the one which will be written in the length field)
     * @param messageId the identification of the type of the mesage
     * @return the buffer containing the message length and id
     */
    private static ByteBuffer generateRequestBuffer(int messageLength, int messageId){
        ByteBuffer request = ByteBuffer.allocate(messageLength + MESSAGE_LENGTH_FIELD_SIZE);
        request.putInt(messageLength);
        request.put((byte) messageId);
        return request;
    }

    public static ByteBuffer createHave(int pieceIndex) {
        ByteBuffer haveBuffer = generateRequestBuffer(HAVE_LENGTH, RequestTypes.HAVE.getId());
        haveBuffer.putInt(pieceIndex);
        return haveBuffer;
    }
}
