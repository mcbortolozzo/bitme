package main.peer;

import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import org.mockito.internal.util.collections.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by marcelo on 07/11/16.
 */
public class Peer{

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    private PeerConnection peerConnection;

    private TorrentFile torrentFile;
    private HashId otherPeerId;
    private HashId localPeerId;

    private PeerProtocolStateManager stateManager = new PeerProtocolStateManager();
    private Bitfield bitfield;

    /**
     * Constructor used when connection is first received, still not knowing to which torrent file it corresponds
     * @param socket the socket created with the acceptor
     * @param selector selector used to register the connection for read and write
     * @throws IOException thrown from peerConnection
     */
    public Peer(SocketChannel socket, Selector selector) throws IOException {
        this.peerConnection = new PeerConnection(socket, selector, this);
    }

    /**
     * Constructor used when this client creates the connection, so it already knows which file it's using
     * @param selector selector used to register the connection for read and write
     * @param torrentFile the file to which this peer will be associated
     * @param destAddr the address of the remote peer used for connection
     * @throws IOException thrown from peerConnection
     */
    public Peer(Selector selector, TorrentFile torrentFile, SocketAddress destAddr) throws IOException {
        this.torrentFile = torrentFile;
        this.localPeerId = torrentFile.getPeerId();
        this.bitfield = new Bitfield(torrentFile);
        this.peerConnection = new PeerConnection(selector, destAddr, this);
    }

    /**
     * static method generating a random peer Id
     * @return a random peer id according to structure used in the bittorrent protocol
     */
    public static HashId generatePeerId() throws IOException {
        String peerId;
        peerId = "-" + Client.CLIENT_ID + Client.CLIENT_VERSION + "-";
        byte[] randomBytes = new byte[12];
        new SecureRandom().nextBytes(randomBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(peerId.getBytes());
        outputStream.write(randomBytes);
        return new HashId(outputStream.toByteArray());
    }

    /**
     * Executes the requests generated when receiving a message, launches it as a new thread in a pool
     * @param requests list of requests received
     */
    public void process(List<TorrentRequest> requests){
        for(TorrentRequest req : requests){
            req.setPeer(this);
            executorService.execute(req);
        }
    }

    /**
     * adds message to connection output buffer
     * @param message the buffer to be written
     */
    public void sendMessage(ByteBuffer message){
        peerConnection.addToBuffer(message);
    }

    /**
     * Creates and sends a handshake message based on the information this peer has
     */
    public void sendHandshake(){
        ByteBuffer message = TorrentProtocolHelper.createHandshake(this.torrentFile.getTorrentId(), this.getLocalPeerId());
        this.sendMessage(message);
    }

    public void setTorrentFile(TorrentFile torrentFile) {
        this.bitfield = new Bitfield(torrentFile);
        this.torrentFile = torrentFile;
    }

    /**
     * @return the peer id associated with the local client, file and connection
     */
    public HashId getLocalPeerId() {
        return localPeerId;
    }

    public void setLocalPeerId(HashId localPeerId) {
        this.localPeerId = localPeerId;
    }

    public HashId getOtherPeerId() {
        return this.otherPeerId;
    }

    /**
     * adds the information about the remote peer id and sets the handshake as complete
     * @param peerId the remote peer id
     */
    public void setOtherPeerId(HashId peerId) {
        this.stateManager.setHandshakeDone(true);   //setting the other peer id can only be done through the handshake in this implementation
        this.otherPeerId = peerId;
    }

    public void setHavePiece(int pieceIndex) {
        this.bitfield.setHavePiece(pieceIndex);
    }

    public boolean hasPiece(int pieceIndex) {
        return this.bitfield.checkHavePiece(pieceIndex);
    }
}
