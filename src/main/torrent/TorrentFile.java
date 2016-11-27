package main.torrent;

import com.hypirion.bencode.BencodeReadException;
import main.peer.Peer;
import main.torrent.file.TorrentBlock;
import main.torrent.file.TorrentFileInfo;
import main.tracker.TrackerHelper;
import main.tracker.TrackerQueryResult;
import java.io.IOException;
import java.util.TimerTask;
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

    private final ScheduledExecutorService trackerExecutor = Executors.newScheduledThreadPool(1);

    //TODO remove this constructor, keep only the other I guess
    public TorrentFile(HashId torrentId) throws IOException {
        this.torrentId = torrentId;
        this.peerId = Peer.generatePeerId();
        this.pieceCount = 2000; //TODO remove this

    }

    public TorrentFile(String filePath, TorrentFileInfo fileInfo) throws IOException, BencodeReadException {
        this.filePath = filePath;
        this.fileInfo = fileInfo;
        this.torrentId = new HashId(fileInfo.getInfoHash());
        this.peerId = Peer.generatePeerId();
        this.pieceCount = this.fileInfo.getPieceCount();
    }

    public TorrentBlock getBlockInfo(int index, int begin, int length){
        return this.fileInfo.getFileBlock(index, begin, length);
    }

    public HashId getTorrentId() {
        return torrentId;
    }

    public int getPieceCount() { return this.pieceCount;
    }

    public HashId getPeerId() {
        return peerId;

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

    private void scheduleTrackerUpdate(Long delay, TimeUnit unit) {
        this.trackerExecutor.schedule(new TrackerUpdater(), delay, unit);
    }

    public void retrieveTrackerData(TrackerHelper.Event event) throws IOException, BencodeReadException {
        String trackerRequest = TrackerHelper.generateTrackerRequest(this.torrentId, event, this.fileInfo.getTrackerAnnounce());
        String result = TrackerHelper.sendTrackerRequest(trackerRequest);
        TrackerQueryResult trackerResult = new TrackerQueryResult(result);
        scheduleTrackerUpdate(5l, TimeUnit.SECONDS);
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
