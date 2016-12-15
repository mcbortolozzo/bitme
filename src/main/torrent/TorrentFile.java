package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.file.BlockPieceManager;
import main.torrent.file.TorrentBlock;
import main.torrent.file.TorrentFileInfo;
import main.tracker.TrackerHelper;
import main.tracker.TrackerPeerInfo;
import main.tracker.TrackerQueryResult;
import main.util.Messages;

import java.io.IOException;
import java.net.ConnectException;
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

    private static final Long TRACKER_ANNOUNCE_PERIOD = 1800l; // in seconds
    private static final Long TRACKER_ERROR_START_PERIOD = 1l;
    private Long nextTrackerErrorDelay = TRACKER_ERROR_START_PERIOD;

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

    private List<Peer> peers = new LinkedList<>();
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
    }

    public synchronized void addPeer(Peer p) {
        this.peers.add(p);
        this.chokingAlgorithm.updatePeers(this.peers);
        this.pieceSelectionAlgorithm.updatePeers(this.peers);
    }

    public synchronized void removePeer(Peer p) {
        this.peers.remove(p);
        this.chokingAlgorithm.updatePeers(this.peers);
        /*for (int i = 0; i < getBitfield().getBitfieldLength(); i++) {
            if(p.hasPiece(i)) {
                pieceDistribution.get(i).remove(p);
                pieceQuantity.set(i, pieceQuantity.get(i) - 1);
            }
        }
        this.pieceSelectionAlgorithm.updatePieceDistribution(pieceDistribution);
        this.pieceSelectionAlgorithm.updatePieceQuantity(pieceQuantity);*/
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
        return this.getPieceCount() - this.getBitfield().getBitfield().cardinality()*this.getFileInfo().getPieceSize();
    }

    public TorrentFileInfo getFileInfo() { return fileInfo; }

    public void scheduleTrackerUpdate(Long delay, TimeUnit unit, TrackerHelper.Event event) {
        this.scheduledExecutor.schedule(new TrackerUpdater(event), delay, unit);
    }

    public List<Peer> getPeers() {return this.peers;}

    /**
     * Update tracker information, and retrieve torrent data
     * @param event type of event announced to tracker: STARTED, STOPPED, UNSPECIFIED
     * @throws IOException
     * @throws BencodeReadException fail when decoding string received from tracker
     */
    public void retrieveTrackerData(TrackerHelper.Event event) throws IOException, BencodeReadException {
        if(this.fileInfo.getTrackerAnnounce() != null) {
            String trackerRequest = TrackerHelper.generateTrackerRequest(this.torrentId, event, this.fileInfo.getTrackerAnnounce());
            try {
                byte[] result = TrackerHelper.sendTrackerRequest(trackerRequest);
                TrackerQueryResult trackerResult = new TrackerQueryResult(result);
                logger.log(Level.FINE, Messages.TRACKER_CONNECT_SUCCCESS.getText());
                //connect to peers
                if(!trackerResult.isFailure() && trackerResult.getPeerInfo() != null){
                    connectToPeers(trackerResult.getPeerInfo());
                }
                //reschedule tracker request
                resetTrackerDelay();
                if(trackerResult.getInterval() != null){
                    scheduleTrackerUpdate(trackerResult.getInterval(), TimeUnit.SECONDS, TrackerHelper.Event.UNSPECIFIED);
                } else {
                    scheduleTrackerUpdate(TRACKER_ANNOUNCE_PERIOD, TimeUnit.SECONDS, TrackerHelper.Event.UNSPECIFIED);
                }
            } catch(ConnectException e){
                logger.log(Level.INFO, Messages.TRACKER_UNREACHABLE.getText() + " - " + this.fileInfo.getTrackerAnnounce() + " repeating in: " + this.nextTrackerErrorDelay + " seconds");
                increaseTrackerDelay();
                scheduleTrackerUpdate(this.nextTrackerErrorDelay, TimeUnit.SECONDS, TrackerHelper.Event.UNSPECIFIED);
            }
        }
    }

    private void increaseTrackerDelay() {
        this.nextTrackerErrorDelay = Math.min(this.nextTrackerErrorDelay * 2, TRACKER_ANNOUNCE_PERIOD);
    }

    private void resetTrackerDelay() {
        this.nextTrackerErrorDelay = TRACKER_ERROR_START_PERIOD;
    }

    /**
     * connect to new peers received from tracker
     * @param peers list of all peers announced by tracker, to be filtered before use
     */
    private void connectToPeers(TrackerPeerInfo peers){
        List<TrackerPeerInfo.PeerTrackerData> newPeers = getNewPeers(peers);
        for(TrackerPeerInfo.PeerTrackerData peer : newPeers){
            try {
                new Peer(this.selector, this, new InetSocketAddress(peer.peerIp, Math.toIntExact(peer.peerPort)));
            } catch (IOException e) {
                logger.log(Level.FINE, Messages.FAILED_CONNECT_PEER.getText() + " - " + peer.peerIp + ":" + peer.peerPort);
            }
        }
    }

    /**
     * filters the list of peers, according to the already existing ones
     * @param peers list of all peers received from tracker
     * @return list of peers without an existing connection
     */
    private List<TrackerPeerInfo.PeerTrackerData> getNewPeers(TrackerPeerInfo peers) {
        List<TrackerPeerInfo.PeerTrackerData> newPeers = new LinkedList<>();
        for(TrackerPeerInfo.PeerTrackerData peer : peers.getPeers()){
            if(this.getPeerByIpAndPort(peer.peerIp, peer.peerPort) == null){
                newPeers.add(peer);
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
        scheduledExecutor.shutdown();
        TorrentManager.getInstance().removeTorrent(this.getTorrentId());
    }

    private class TrackerUpdater implements Runnable {

        private TrackerHelper.Event event;

        public TrackerUpdater(TrackerHelper.Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                retrieveTrackerData(event);
            } catch (IOException e) { //TODO treat these exceptions
                e.printStackTrace();
            } catch (BencodeReadException e) {
                e.printStackTrace();
            }
        }
    }


}
