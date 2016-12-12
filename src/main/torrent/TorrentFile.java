package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.file.BlockPieceManager;
import main.torrent.file.TorrentBlock;
import main.torrent.file.TorrentFileInfo;
import main.tracker.TrackerHelper;
import main.tracker.TrackerPeerDictionary;
import main.tracker.TrackerPeerInfo;
import main.tracker.TrackerQueryResult;
import main.util.Messages;
import org.omg.SendingContext.RunTime;
import sun.util.logging.PlatformLogger;

import java.io.FileNotFoundException;
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

import static main.tracker.TrackerPeerInfo.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentFile {

    private static final Long TRACKER_ANNOUNCE_PERIOD = 1800l; // in seconds

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Selector selector;

    private final HashId peerId;
    private HashId torrentId;

    private TorrentFileInfo fileInfo;
    private String filePath;

    private Long pieceSize;
    private int pieceCount;

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
        this.pieceSelectionAlgorithm = new PieceSelectionAlgorithm(this.blockPieceManager, fileInfo);
        this.scheduledExecutor.scheduleAtFixedRate(this.chokingAlgorithm, 0, ChokingAlgorithm.RUN_PERIOD, TimeUnit.MILLISECONDS);
        this.scheduledExecutor.scheduleAtFixedRate(this.pieceSelectionAlgorithm, 0, PieceSelectionAlgorithm.RUN_PERIOD, TimeUnit.MILLISECONDS);
    }

    public synchronized void addPeer(Peer p) {
        this.peers.add(p);
        this.chokingAlgorithm.updatePeers(this.peers);
        this.pieceSelectionAlgorithm.updatePeers(this.peers);
    }

    public void removePeer(Peer p) {
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

    //TODO get uploaded, downloaded and left according to peers
    public int getUploaded() {
        return 0;
    }

    public int getDownloaded(){
        return 0;
    }

    public int getLeft(){
        return 0;
    }

    public TorrentFileInfo getFileInfo() { return fileInfo; }

    private void scheduleTrackerUpdate(Long delay, TimeUnit unit) {
        this.scheduledExecutor.schedule(new TrackerUpdater(), delay, unit);
    }

    public void retrieveTrackerData(TrackerHelper.Event event) throws IOException, BencodeReadException {
        if(this.fileInfo.getTrackerAnnounce() != null) {
            String trackerRequest = TrackerHelper.generateTrackerRequest(this.torrentId, event, this.fileInfo.getTrackerAnnounce());
            String result = TrackerHelper.sendTrackerRequest(trackerRequest);
            TrackerQueryResult trackerResult = new TrackerQueryResult(result);
            //connect to peers
            if(!trackerResult.isFailure() && trackerResult.getPeerInfo() != null){
                connectToPeers(trackerResult.getPeerInfo());
            }
            //reschedule for next get
            if(trackerResult.getInterval() != null){
                scheduleTrackerUpdate(trackerResult.getInterval(), TimeUnit.SECONDS);
            } else {
                scheduleTrackerUpdate(TRACKER_ANNOUNCE_PERIOD, TimeUnit.SECONDS);
            }
        }
    }

    private void connectToPeers(TrackerPeerInfo peers){
        List<TrackerPeerInfo.PeerTrackerData> newPeers = getNewPeers(peers);
        for(TrackerPeerInfo.PeerTrackerData peer : newPeers){
            try {
                Peer p = new Peer(this.selector, this, new InetSocketAddress(peer.peerIp, Math.toIntExact(peer.peerPort)));
                this.addPeer(p);
            } catch (IOException e) {
                logger.log(Level.FINE, Messages.FAILED_CONNECT_PEER.getText() + " - " + peer.peerIp + ":" + peer.peerPort);
            }
        }
    }

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

    private class TrackerUpdater implements Runnable {

        @Override
        public void run() {
            try {
                retrieveTrackerData(TrackerHelper.Event.UNSPECIFIED);
            } catch (IOException e) { //TODO treat these exceptions
                e.printStackTrace();
            } catch (BencodeReadException e) {
                e.printStackTrace();
            }
        }
    }


}
