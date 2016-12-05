package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.file.TorrentBlock;
import main.torrent.file.TorrentFileInfo;
import main.tracker.TrackerHelper;
import main.tracker.TrackerQueryResult;
import org.omg.SendingContext.RunTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentFile {

    private final HashId peerId;
    private HashId torrentId;

    private TorrentFileInfo fileInfo;
    private String filePath;

    private Long pieceSize;
    private int pieceCount;

    private Bitfield bitfield;

    private List<Peer> peers = new LinkedList<>();
    private ChokingAlgorithm chokingAlgorithm = new ChokingAlgorithm();

    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    public TorrentFile(String filePath, TorrentFileInfo fileInfo) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        this.filePath = filePath;
        this.fileInfo = fileInfo;
        this.torrentId = new HashId(fileInfo.getInfoHash());
        this.peerId = Peer.generatePeerId();
        this.pieceCount = this.fileInfo.getPieceCount();
        this.pieceSize = this.fileInfo.getPieceSize();
        this.updateBitfield();
        this.scheduledExecutor.scheduleAtFixedRate(this.chokingAlgorithm, 0, ChokingAlgorithm.RUN_PERIOD, TimeUnit.MILLISECONDS);
    }

    public synchronized void addPeer(Peer p) {
        this.peers.add(p);
        this.chokingAlgorithm.updatePeers(this.peers);
    }

    public void removePeer(Peer p) {
        this.peers.remove(p);
        this.chokingAlgorithm.updatePeers(this.peers);
    }

    public TorrentBlock getBlockInfo(int index, int begin, int length){
        return this.fileInfo.getFileBlock(index, begin, length);
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
        this.bitfield = new Bitfield(this.getPieceCount());
        TorrentBlock tb = this.fileInfo.getFileBlock(0, 0, Math.toIntExact(this.fileInfo.getLength()));
        ByteBuffer pieceBuffer = tb.readFileBlock();
        byte[] piece = new byte[Math.toIntExact(this.pieceSize)];
        for(int i = 0; i < this.getPieceCount() - 1; i ++){ //read all pieces except for last one
            pieceBuffer.get(piece);
            if(this.fileInfo.isPieceValid(piece, i)){
                this.bitfield.setHavePiece(i);
            }
        }
        //and for the last piece
        piece = new byte[pieceBuffer.remaining()];
        pieceBuffer.get(piece);
        if(this.fileInfo.isPieceValid(piece, this.getPieceCount() - 1)){
            this.bitfield.setHavePiece(this.getPieceCount() - 1);
        }
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
            scheduleTrackerUpdate(5l, TimeUnit.SECONDS);
        }
    }

    public ScheduledExecutorService getExecutor() {
        return this.scheduledExecutor;
    }

    private class TrackerUpdater implements Runnable {

        @Override
        public void run() {
            try {
                retrieveTrackerData(TrackerHelper.Event.UNSPECIFIED);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BencodeReadException e) {
                e.printStackTrace();
            }
        }
    }


}
