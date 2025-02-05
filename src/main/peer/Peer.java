package main.peer;

import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.file.TorrentBlock;
import main.torrent.protocol.RequestTypes;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.tracker.TrackerPeerInfo;
import main.util.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class Peer{

    private static final Long MEASUREMENT_PERIOD = 1000l; // in milliseconds
    private static final Long KEEP_ALIVE_PERIOD = 5l; // in seconds

    Logger logger = Logger.getLogger(Peer.class.getName());

    private PeerConnection peerConnection;
    private String peerIp;
    private int peerPort;

    private TorrentFile torrentFile;
    private HashId otherPeerId;
    private HashId localPeerId;

    private SpeedRateCalculations speedRateCalculations = new SpeedRateCalculations();
    ScheduledFuture rateCalculationFuture;
    ScheduledFuture keepAliveFuture;
    private int uploaded = 0;
    private LinkedList<Integer> uploadBytesLog = new LinkedList<>();
    private int downloaded = 0;
    private LinkedList<Integer> downloadBytesLog = new LinkedList<>();

    private PeerProtocolStateManager stateManager;
    private Bitfield bitfield;

    private Date lastContact;

    private boolean handshakeSent = false;

    /**
     * Constructor used when connection is first received, still not knowing to which torrent file it corresponds
     * @param socket the socket created with the acceptor
     * @param selector selector used to register the connection for read and write
     * @throws IOException thrown from peerConnection
     */
    public Peer(SocketChannel socket, Selector selector) throws IOException {
        logger.log(Level.FINE, Messages.PEER_CONNECTION_ACCEPT.getText());
        this.peerConnection = new PeerConnection(socket, selector, this);
    }

    /**
     * Constructor used when this client creates the connection, so it already knows which file it's using
     * @param selector selector used to register the connection for read and write
     * @param torrentFile the file to which this peer will be associated
     * @param destAddr the address of the remote peer used for connection
     * @throws IOException thrown from peerConnection
     */
    public Peer(Selector selector, TorrentFile torrentFile, InetSocketAddress destAddr) throws IOException {
        logger.log(Level.INFO, Messages.PEER_CONNECTION_AUTO_CREATE.getText() + " - " + destAddr);
        this.peerIp = destAddr.getHostName();
        this.peerPort = destAddr.getPort();
        this.localPeerId = torrentFile.getPeerId();
        this.peerConnection = new PeerConnection(selector, destAddr, this);
        this.setTorrentFile(torrentFile);
        this.lastContact = Date.from(Instant.now());
        this.sendHandshake(null);
    }

    private void launchScheduledEvents() {
        rateCalculationFuture = this.torrentFile.getExecutor().scheduleAtFixedRate(speedRateCalculations, 0, MEASUREMENT_PERIOD, TimeUnit.MILLISECONDS);
        keepAliveFuture = this.torrentFile.getExecutor().scheduleAtFixedRate(new KeepAliveMessage(), 0, KEEP_ALIVE_PERIOD, TimeUnit.SECONDS);
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
     * @param req list of requests received
     */
    public void process(TorrentRequest req){
        req.setPeer(this);
        TorrentManager.executorService.execute(req);
        this.lastContact = Date.from(Instant.now());
    }

    /**
     * adds message to connection output buffer
     * @param message the buffer to be written
     */
    public void sendMessage(ByteBuffer message){
        peerConnection.addToBuffer(new TorrentRequest.OutboundMessage(message, null));
    }

    public void sendReactiveMessage(ByteBuffer message, TorrentRequest request){
        peerConnection.addToBuffer(new TorrentRequest.OutboundMessage(message, request));
    }

    /**
     * Creates and sends a handshake message based on the information this peer has
     * @param request
     */
    public void sendHandshake(TorrentRequest request){
        logger.log(Level.FINE, Messages.SEND_HANDSHAKE.getText());
        this.handshakeSent = true;
        ByteBuffer message = TorrentProtocolHelper.createHandshake(this.torrentFile.getTorrentId(), this.getLocalPeerId());
        this.sendReactiveMessage(message, request);
    }

    /**
     * Sends the local bitfield to the remote peer
     * @param request
     */
    public void sendBitfield(TorrentRequest request) {
        logger.log(Level.FINE, Messages.SEND_BITFIELD.getText());
        ByteBuffer message = TorrentProtocolHelper.createBitfield(this.torrentFile.getBitfield());
        this.sendReactiveMessage(message, request);
    }

    /**
     * Sends the state change message, Choke, Unchoke, Interested, Not Interested
     * @param state the type of state change used
     */
    public void sendStateChange(RequestTypes state){
        logger.log(Level.FINE, Messages.SEND_STATE_CHANGE.getText() + " - " + state.toString());
        ByteBuffer message = TorrentProtocolHelper.createStateChangeMessage(state);
        this.sendMessage(message);
    }

    public void sendHave(int pieceIndex) {
        logger.log(Level.FINE, Messages.SEND_HAVE.getText() + " - " + pieceIndex);
        ByteBuffer message = TorrentProtocolHelper.createHave(pieceIndex);
        this.sendMessage(message);
    }

    public void sendCancelMessage(int pieceIndex, int begin, int length) {
        logger.log(Level.FINE, Messages.SEND_CANCEL.getText() + " - index " + pieceIndex + " - begin " + begin + " peer - " + this.getPeerIp());
        ByteBuffer message = TorrentProtocolHelper.createCancel(pieceIndex, begin, length);
        this.sendMessage(message);
    }

    /**
     * When this torrent is the target of the connection, it only knows which torrent to use when it receives the handshake
     * @param torrentFile the torrent used on the handshake, only valid torrent files will be used (validation in handshake)
     */
    public synchronized void setTorrentFile(TorrentFile torrentFile) {
        if(this.torrentFile == null) {
            this.bitfield = new Bitfield(torrentFile);
            this.torrentFile = torrentFile;
            this.torrentFile.addPeer(this);
            this.stateManager = new PeerProtocolStateManager(this.torrentFile.getBitfield(), this.bitfield);
            this.launchScheduledEvents();
        }
    }

    /**
     * @return the peer id associated with the local client, file and connection
     */
    public HashId getLocalPeerId() {
        return localPeerId;
    }

    /**
     * Updates the local peer id, according to the torrent file which this peer is associated to
     * @param localPeerId when creating the torrent this will be set automatically, but when receiving connection this
     *                    will be known only when handshake is received
     */
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

    public String getPeerClient(){
        if (this.getOtherPeerId() != null)
            return this.getOtherPeerId().getClient();
        return null;
    }

    public void setState(RequestTypes type) {
        this.stateManager.setState(type);
    }

    public Bitfield getBitfield() {
        return this.bitfield;
    }

    public void updateBitfield(BitSet bitfield) {
        this.bitfield.updateBitfield(bitfield);
        this.updateInterested();
        torrentFile.updateAvailablePieces(this.bitfield, this);
    }

    public boolean updateInterested() {
        boolean prevInterested = this.stateManager.getAmInterested();
        boolean currentInterested = this.stateManager.updateInterested();
        if(currentInterested != prevInterested){
            if(currentInterested){
                this.sendStateChange(RequestTypes.INTERESTED);
            } else {
                this.sendStateChange(RequestTypes.NOT_INTERESTED);
            }
        }
        return currentInterested;
    }

    public boolean isPeerChoking() {
        return this.stateManager.isPeerChoking();
    }

    public boolean isPeerInterested() {
        return this.stateManager.isPeerInterested();
    }

    public void setHavePiece(int pieceIndex) {
        this.bitfield.setHavePiece(pieceIndex);
        this.updateInterested();
        torrentFile.updatePiecesFromHave(pieceIndex, this);
    }

    public boolean hasPiece(int pieceIndex) {
        return this.bitfield.checkHavePiece(pieceIndex);
    }

    public boolean isHandshakeSent() {
        return handshakeSent;
    }

    /**
     * Fetch a data block from the local file in order to send it to other peer
     * @param pieceIndex the index of the piece
     * @param begin the offset inside the piece block, in bytes
     * @param length the length in bytes to be fetched
     * @return the bytebuffer containing the data to be sent
     * @throws IOException File read failures may cause this
     */
    public ByteBuffer retrieveDataBlock(int pieceIndex, int begin, int length) throws IOException {
        TorrentBlock tb = this.torrentFile.getBlockInfo(pieceIndex, begin, length);
        return tb.readFileBlock();
    }

    /**
     * Writes a buffer to a file from the data received from other peer
     * @param pieceIndex the index of the piece
     * @param begin the offset inside the piece block, in bytes
     * @param block the block with the bytes to be written
     * @throws IOException file or buffer read error
     */
    public void writeDataBlock(int pieceIndex, int begin, byte[] block) throws IOException {
        ByteBuffer outBuffer = ByteBuffer.allocate(block.length);
        outBuffer.put(block);
        outBuffer.flip();
        TorrentBlock tb = this.torrentFile.getBlockInfo(pieceIndex, begin, block.length);
        tb.writeFileBlock(outBuffer);
    }

    public boolean verifyPieceHash(int pieceIndex) throws IOException, NoSuchAlgorithmException {
        TorrentBlock tb = this.torrentFile.getBlockInfo(pieceIndex, 0, Math.toIntExact(this.torrentFile.getPieceSize()));
        ByteBuffer pieceBuffer = tb.readFileBlock();
        if(torrentFile.getFileInfo().isPieceValid(pieceBuffer.array(), pieceIndex)){
            this.torrentFile.setHavePiece(pieceIndex);
            return true;
        }
        return false;
    }

    /**
     * Closes this connection and updates the torrent file by removing the peer from it
     */
    public void shutdown() {
        logger.log(Level.INFO, Messages.PEER_SHUTDOWN.getText() + " - other peer: " + this.getOtherPeerId() + " - " + this.getPeerIp());
        if(rateCalculationFuture != null)
            rateCalculationFuture.cancel(true);
        if(this.peerConnection != null) {
            this.peerConnection.shutdown();
        }
        if(this.torrentFile != null)
            this.torrentFile.removePeer(this);
    }

    public synchronized void addUploaded(int amount) {
        this.uploaded += amount;
        this.torrentFile.updateUploaded(amount);
    }

    public synchronized int getUploaded(){ return this.uploaded; }

    public synchronized void addDownloaded(int amount){
        this.downloaded += amount;
        this.torrentFile.updateDownloaded(amount);
    }

    public synchronized int getDownloaded(){ return this.downloaded; }

    public String getPeerIp() {
        return peerIp;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public List<Integer> getUDowloadLog() { return this.downloadBytesLog;}

    public List<Integer> getUploadLog() { return this.uploadBytesLog;}

    public void receivePieceBlock(int pieceIndex, int begin, byte[] block) throws IOException, NoSuchAlgorithmException {
        this.addDownloaded(block.length);
        boolean pieceDone = this.torrentFile.receivePieceBlock(pieceIndex, begin, block, this);
        if(pieceDone){
            this.torrentFile.setHavePiece(pieceIndex);
            this.sendHave(pieceIndex);
            this.updateInterested();
        }
    }

    public Boolean isPeerConnectionNull(){
        return this.peerConnection == null;
    }

    public void cancelRequest(int pieceIndex, int begin) {
        this.peerConnection.cancelRequest(pieceIndex, begin);
    }

    public float getOtherPeerProgress() {
        return 100*(float)this.getBitfield().getBitfield().cardinality()/this.torrentFile.getPieceCount();
    }

    private class KeepAliveMessage implements Runnable {
        @Override
        public void run() {
            ByteBuffer message = TorrentProtocolHelper.createKeepAlive();
            sendMessage(message);
        }
    }

    private class SpeedRateCalculations implements Runnable {

        private int lastUpload = 0;
        private int lastDownload = 0;
        @Override
        public void run() {
            int localDownloaded = getDownloaded();
            int localUploaded = getUploaded();
            logger.log(Level.FINE, "Peer - " + getOtherPeerId() + " - download speed - " + this.lastDownload);
            downloadBytesLog.push(localDownloaded - this.lastDownload);
            this.lastDownload = localDownloaded;
            uploadBytesLog.push(localUploaded - this.lastUpload);
            this.lastUpload = localUploaded;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Peer){
            Peer other = (Peer) obj;
            if(other.getOtherPeerId() != null && this.getOtherPeerId() != null){
                return this.getOtherPeerId().equals(other.getOtherPeerId());
            }else if(this.getPeerIp() != null && other.getPeerIp() != null){
                return this.getPeerIp().equals(other.getPeerIp()) && this.getPeerPort() == other.getPeerPort();
            }
        }
        return false;
    }

    public boolean isEquivalentPeer(TrackerPeerInfo.PeerTrackerData other){
        if(this.getPeerIp() != null && other.peerIp != null && other.peerPort != null){
            return this.getPeerIp().equals(other.peerIp) && other.peerPort.intValue() == this.getPeerPort();
        }
        return false;
    }

}
