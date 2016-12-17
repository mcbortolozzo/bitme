package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.file.BlockPieceManager;
import main.torrent.file.TorrentBlock;
import main.torrent.file.TorrentFileInfo;
import main.tracker.Tracker;
import main.tracker.http.HttpTracker;
import main.tracker.http.HttpTrackerHelper;
import main.tracker.TrackerPeerInfo;
import main.tracker.udp.UdpTracker;
import main.util.Messages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentFile {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Selector selector;

    private final HashId peerId;
    private HashId torrentId;

    private TorrentFileInfo fileInfo;
    private String filePath;

    private Long pieceSize;
    private int pieceCount;
    private int downloaded = 0;
    private int uploaded = 0;

    private Bitfield bitfield;

    private List<Tracker> trackers = new LinkedList<>();

    private List<Peer> peers = new LinkedList<>();
    private Set<TrackerPeerInfo.PeerTrackerData> trackedPeers = new HashSet<>();
    private ChokingAlgorithm chokingAlgorithm = new ChokingAlgorithm();
    private PieceSelectionAlgorithm pieceSelectionAlgorithm;
    private BlockPieceManager blockPieceManager;

    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    public TorrentFile(String filePath, TorrentFileInfo fileInfo, Selector selector) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        this.filePath = filePath;
        this.fileInfo = fileInfo;
        this.torrentId = new HashId(fileInfo.getInfoHash());
        this.peerId = Peer.generatePeerId();
        this.pieceCount = this.fileInfo.getPieceCount();
        this.pieceSize = this.fileInfo.getPieceSize();
        this.selector = selector;
        this.updateBitfield();
        this.blockPieceManager = new BlockPieceManager(pieceCount, pieceSize, this.fileInfo.getLength(), bitfield, fileInfo);
        this.pieceSelectionAlgorithm = new PieceSelectionAlgorithm(this.blockPieceManager, fileInfo, bitfield);
        this.scheduledExecutor.scheduleAtFixedRate(this.chokingAlgorithm, 0, ChokingAlgorithm.RUN_PERIOD, TimeUnit.MILLISECONDS);
        this.scheduledExecutor.scheduleAtFixedRate(this.pieceSelectionAlgorithm, 0, PieceSelectionAlgorithm.RUN_PERIOD, TimeUnit.MILLISECONDS);
        this.initTrackers();
    }

    private void initTrackers() {
        List<String> httpTrackers = this.fileInfo.getHttpTrackers();
        List<String> udpTrackers = this.fileInfo.getUdpTrackers();
        for(String trackerAnnounce : httpTrackers){
            this.trackers.add(new HttpTracker(this, trackerAnnounce));
        }
        for(String trackerAnnounce : udpTrackers){
            this.trackers.add(new UdpTracker(this, trackerAnnounce));
        }
        this.trackers.forEach(main.tracker.Tracker::start);
    }

    public synchronized void addPeer(Peer p) {
        this.peers.add(p);
        this.chokingAlgorithm.updatePeers(this.peers);
        this.pieceSelectionAlgorithm.updatePeers(this.peers);
    }

    public synchronized void removePeer(Peer p) {
        this.peers.remove(p);
        this.chokingAlgorithm.updatePeers(this.peers);
        this.pieceSelectionAlgorithm.updatePeers(peers);
    }

    public TorrentBlock getBlockInfo(int index, int begin, int length){
        return this.fileInfo.getFileBlock(index, begin, length);
    }

    public void updateAvailablePieces(Bitfield bitfield, Peer p) {
        pieceSelectionAlgorithm.updateAvailablePieces(bitfield, p);
    }

    public synchronized void updatePiecesFromHave(int index, Peer p) {
        pieceSelectionAlgorithm.updatePiecesFromHave(index, p);
    }

    public HashId getTorrentId() {
        return torrentId;
    }

    public int getPieceCount() { return this.pieceCount; }

    public long getPieceSize() { return this.pieceSize; }

    public HashId getPeerId() {
        return peerId;
    }

    private void updateBitfield() throws IOException, NoSuchAlgorithmException {
        this.fileInfo.verifyAndAllocateFiles();
        this.bitfield = new Bitfield(this.getPieceCount());
        List<TorrentBlock> pieces = new LinkedList<>();
        for(int i = 0; i < this.getPieceCount(); i++){
            pieces.add(this.fileInfo.getFileBlock(i, 0, Math.toIntExact(this.fileInfo.getPieceSize())));
        }
        int pieceIndex = 0;
        for(TorrentBlock tb : pieces){ //read all pieces except for last one
            ByteBuffer pieceBuffer = tb.readFileBlock();
            if(this.fileInfo.isPieceValid(pieceBuffer.array(), pieceIndex)){
                this.bitfield.setHavePiece(pieceIndex);
            }
            pieceIndex++;
        }
    }

    public void setHavePiece(int pieceIndex) {
        this.bitfield.setHavePiece(pieceIndex);
    }

    public Bitfield getBitfield() {
        return bitfield;
    }

    public synchronized void updateDownloaded(int amount){
        downloaded += amount;
    }

    public int getDownloaded(){
        return downloaded;
    }

    public synchronized void updateUploaded(int amount){
        uploaded += amount;
    }

    public int getUploaded(){
        return uploaded;
    }

    public Long getLeft(){
        return (this.getPieceCount() - this.getBitfield().getBitfield().cardinality())*this.getFileInfo().getPieceSize();
    }

    public TorrentFileInfo getFileInfo() { return fileInfo; }

    public List<Peer> getPeers() {return this.peers;}


    /**
     * connect to new peers received from tracker
     */
    private synchronized void connectToNewPeers(){
        List<TrackerPeerInfo.PeerTrackerData> newPeers = this.getNewPeers();
        for(TrackerPeerInfo.PeerTrackerData newPeer : newPeers){
            try {
                new Peer(this.selector, this, new InetSocketAddress(newPeer.peerIp, Math.toIntExact(newPeer.peerPort)));
            } catch (IOException e) {
                logger.log(Level.FINE, Messages.FAILED_CONNECT_PEER.getText() + " - " + newPeer.peerIp + ":" + newPeer.peerPort);
            }
        }
    }

    public synchronized void updateTrackedPeers(TrackerPeerInfo peerInfo) {
        this.trackedPeers.addAll(peerInfo.getPeers());
        this.connectToNewPeers();
    }

    /**
     * filters the list of peers, according to the already existing ones
     * @return list of peers without an existing connection
     */
    private synchronized List<TrackerPeerInfo.PeerTrackerData> getNewPeers() {
        List<TrackerPeerInfo.PeerTrackerData> newPeers = new LinkedList<>();
        for (TrackerPeerInfo.PeerTrackerData nextTrackedPeer : trackedPeers) {
            if (this.getPeerByIpAndPort(nextTrackedPeer.peerIp, nextTrackedPeer.peerPort) == null) {
                newPeers.add(nextTrackedPeer);
            }
        }
        return newPeers;
    }

    private synchronized Peer getPeerByIpAndPort(String peerIp, Long peerPort) {
        for(Peer p : this.peers){
            if(p.getPeerIp().equals(peerIp) && p.getPeerPort() == peerPort)
                return p;
        }
        return null;
    }

    public ScheduledExecutorService getExecutor() {
        return this.scheduledExecutor;
    }

    public boolean receivePieceBlock(int pieceIndex, int begin, byte[] block, Peer p) throws IOException, NoSuchAlgorithmException {
        return this.blockPieceManager.receiveBlock(pieceIndex, begin, block, p);
    }

    public void start() {
        this.pieceSelectionAlgorithm.setPause(false);
        this.chokingAlgorithm.setPause(false);
    }

    public synchronized void pause() {
        this.pieceSelectionAlgorithm.setPause(true);
        this.chokingAlgorithm.setPause(true);
    }

    public synchronized void shutdown() {
        while(!peers.isEmpty()) {
            peers.get(0).shutdown();
        }
        for(Tracker t: this.trackers){
            t.stop();
        }
        scheduledExecutor.shutdown();
        TorrentManager.getInstance().removeTorrent(this.getTorrentId());
    }

}
