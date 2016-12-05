package main.torrent.protocol;

import main.peer.Bitfield;
import main.torrent.HashId;

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
    public static final int ID_LEN = 20;
    public static final int HANDSHAKE_SIZE = 1+19+8+ID_LEN*2; // pstrlen + protocol + reserved + torrent info hash + peer id
    public static final int STATE_CHANGE_LENGTH = 1; // just the id
    public static final int BITFIELD_INITIAL_LENGHT = 1; // the lenght of a bitfield is variable
    public static final int KEEP_ALIVE_LENGHT = 0; // keep alive requests have no message
    public static final int PIECE_INITIAL_LENGHT = 9; // id + piece index + begin
    public static final int REQUEST_LENGHT = 13; // id + piece index + begin + length
    public static final int CANCEL_LENGHT = 13; // id + piece index + begin + length
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
            } else if(messageBuffer.hasRemaining()){
                messageBuffer.get();
                messageBuffer.compact();
                messageBuffer.flip();
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
    public static ByteBuffer createHandshake(HashId torrentId, HashId peerId) {
        if(torrentId.length() != ID_LEN)
            throw new InvalidParameterException("Invalid torrent Id");
        if(peerId.length() != ID_LEN)
            throw new InvalidParameterException("Invalid peer Id");
        ByteBuffer handshake = ByteBuffer.allocate(HANDSHAKE_SIZE);
        handshake.put((byte) PSTRLEN);
        handshake.put(PROTOCOL_VERSION.getBytes());
        for(int i = 0; i < 8; i ++) handshake.put((byte) 0);   //reserved byte
        handshake.put(torrentId.getBytes());
        handshake.put(peerId.getBytes());
        return handshake;
    }

    /**
     * Creates the bitfield message according to the given bitfield
     * @param bitfield of the torrent
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createBitfield(Bitfield bitfield) {
        int messageLenght = BITFIELD_INITIAL_LENGHT + bitfield.getBitfieldLengthInBytes();
        ByteBuffer bitfieldBuffer = generateRequestBuffer(messageLenght, RequestTypes.BITFIELD.getId());
        bitfieldBuffer.put(bitfield.getBytes());
        return bitfieldBuffer;
    }

    /**
     * Creates the state change message according to the type of the request
     * @param stateRequest type of request that is being made
     * @return the buffer containing the message to be sent
     */
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
        if(messageId != -2) {
            request.put((byte) messageId);
        }
        return request;
    }

    /**
     * Create the have message according to the index of the piece
     * @param pieceIndex index of the piece inside the torrent
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createHave(int pieceIndex) {
        ByteBuffer haveBuffer = generateRequestBuffer(HAVE_LENGTH, RequestTypes.HAVE.getId());
        haveBuffer.putInt(pieceIndex);
        return haveBuffer;
    }

    /**
     * Creates the request message according to the index of the wanted piece, the offset of the block within the piece
     * and the lenght of the block
     * @param pieceIndex index of the desired piece
     * @param begin offset within the piece
     * @param lenght length of the desired block
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createRequest(int pieceIndex, int begin, int lenght) {
        ByteBuffer requestBuffer = generateRequestBuffer(REQUEST_LENGHT, RequestTypes.REQUEST.getId());
        requestBuffer.putInt(pieceIndex);
        requestBuffer.putInt(begin);
        requestBuffer.putInt(lenght);
        return requestBuffer;
    }

    /**
     * Creates the piece message according to the index of the piece to be sent, the offset of the block withing the
     * piece and the sent block
     * @param pieceIndex index of the piece being sent
     * @param begin offset within the piece
     * @param block data block being sent
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createPiece(int pieceIndex, int begin, ByteBuffer block) {
        int messageLength = PIECE_INITIAL_LENGHT + block.capacity();
        ByteBuffer pieceBuffer = generateRequestBuffer(messageLength, RequestTypes.PIECE.getId());
        pieceBuffer.putInt(pieceIndex);
        pieceBuffer.putInt(begin);
        pieceBuffer.put(block);
        return pieceBuffer;
    }

    /**
     * Creates the keep alive message.
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createKeepAlive() {
        return generateRequestBuffer(KEEP_ALIVE_LENGHT, RequestTypes.KEEP_ALIVE.getId());
    }

    /**
     * Creates the cancel message according to the index of the piece that was previously requested, the offset within
     * the piece and the lenght of the block
     * @param pieceIndex index of the piece that was requested
     * @param begin offset withing the piece
     * @param lenght lenght of the block
     * @return the buffer containing the message to be sent
     */
    public static ByteBuffer createCancel(int pieceIndex, int begin, int lenght) {
        ByteBuffer cancelBuffer = generateRequestBuffer(CANCEL_LENGHT, RequestTypes.CANCEL.getId());
        cancelBuffer.putInt(pieceIndex);
        cancelBuffer.putInt(begin);
        cancelBuffer.putInt(lenght);
        return cancelBuffer;
    }
}
