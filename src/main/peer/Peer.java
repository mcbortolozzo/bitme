package main.peer;

import main.Client;
import main.torrent.TorrentFile;
import main.torrent.protocol.RequestTypes;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;

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
    private String otherPeerId;
    private final String localPeerId = generatePeerId();

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
     * Constructor used when this client creates de connection, so it already knows which file it's using
     * @param selector selector used to register the connection for read and write
     * @param torrentFile the file to which this peer will be associated
     * @param destAddr the address of the remote peer used for connection
     * @throws IOException thrown from peerConnection
     */
    public Peer(Selector selector, TorrentFile torrentFile, SocketAddress destAddr) throws IOException {
        this.torrentFile = torrentFile;
        this.bitfield = new Bitfield(torrentFile);
        this.peerConnection = new PeerConnection(selector, destAddr, this);
    }

    /**
     * static method generating a random peer Id
     * @return a random peer id according to structure used in the bittorrent protocol
     */
    private static String generatePeerId(){
        String peerId;
        peerId = "-" + Client.CLIENT_ID + Client.CLIENT_VERSION + "-";
        for(int i = 0; i < 12; i++){
            peerId += new SecureRandom().nextInt(10);
        }
        byte[] bytes = peerId.getBytes();
        return peerId;
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

    /**
     * @return the peer id associated with the local client, file and connection
     */
    public String getLocalPeerId() {
        return localPeerId;
    }

    public void setTorrentFile(TorrentFile torrentFile) {
        this.bitfield = new Bitfield(torrentFile);
        this.torrentFile = torrentFile;
    }

    /**
     * adds the information about the remote peer id and sets the handshake as complete
     * @param peerId the remote peer id
     */
    public void setOtherPeerId(String peerId) {
        this.stateManager.setHandshakeDone(true);   //setting the other peer id can only be done through the handshake in this implementation
        this.otherPeerId = peerId;
    }

    public String getOtherPeerId() {
        return this.otherPeerId;
    }

    public void setHavePiece(int pieceIndex) {
        this.bitfield.setHavePiece(pieceIndex);
    }

    public boolean hasPiece(int pieceIndex) {
        return this.bitfield.checkHavePiece(pieceIndex);
    }
}
