package main.tracker;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.TorrentFile;
import main.tracker.http.HttpTrackerHelper;
import main.util.Messages;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Tracker {

    public enum Event{
        STARTED(2), STOPPED(3), COMPLETED(1), UNSPECIFIED(0);

        public int id;
        Event(int id) {
            this.id = id;
        }
    }

    public enum Field {
        INFO_HASH, PEER_ID, PORT, UPLOADED, DOWNLOADED, LEFT, COMPACT, NO_PEER_ID,
        EVENT, IP, NUMWANT, KEY, TRACKERID;
    }

    protected static ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);
    protected static final Long TRACKER_ANNOUNCE_PERIOD = 1800l; // in seconds
    protected static final Long TRACKER_ERROR_START_PERIOD = 15l;
    protected static final int MAX_ATTEMPT_COUNT = 8;

    Logger logger = Logger.getLogger(this.getClass().getName());

    private int attempt = 0;
    protected String announceAddr;
    protected TorrentFile torrentFile;
    protected Future trackerEvent;

    protected Tracker(TorrentFile torrentFile, String announceAddr) {
        this.announceAddr = announceAddr;
        this.torrentFile = torrentFile;
    }

    public void start() {
        this.scheduleTrackerUpdate(0l, TimeUnit.SECONDS, Event.STARTED);
    }

    public void stop() {
        this.scheduleTrackerUpdate(0l, TimeUnit.SECONDS, Event.STOPPED);
    }

    public void completed() {
        this.scheduleTrackerUpdate(0l, TimeUnit.SECONDS, Event.COMPLETED);
    }

    public void scheduleTrackerUpdate(Long delay, TimeUnit unit, Event event) {
        if(trackerEvent != null && !trackerEvent.isDone())
            trackerEvent.cancel(false);
        trackerEvent = scheduledExecutor.schedule(new TrackerUpdater(event), delay, unit);
    }

    protected void retrieveTrackerData(Event event) throws IOException, BencodeReadException{
        TrackerQueryResult result = sendTrackerAnnounce(event);
        logger.log(Level.FINE, Messages.TRACKER_CONNECT_SUCCCESS.getText());
        //connect to peers
        if(!result.isFailure() && result.getPeerInfo() != null){
            this.torrentFile.updateTrackedPeers(result.getPeerInfo());
        }
        //reschedule tracker request
        resetTrackerDelay();
        if(!event.equals(Event.COMPLETED) && !event.equals(Event.STOPPED)){
            if(result.getInterval() != null){
                scheduleTrackerUpdate(result.getInterval(), TimeUnit.SECONDS, Event.UNSPECIFIED);
            } else {
                scheduleTrackerUpdate(TRACKER_ANNOUNCE_PERIOD, TimeUnit.SECONDS, Event.UNSPECIFIED);
            }
        }
    }

    protected abstract TrackerQueryResult sendTrackerAnnounce(Event event) throws IOException, BencodeReadException;

    private void increaseTrackerDelay() {
        this.attempt = Math.min(this.attempt + 1, MAX_ATTEMPT_COUNT);
    }

    private void resetTrackerDelay() {
        this.attempt = 0;
    }

    private long getTrackerErrorDelay(){
        return (long) (TRACKER_ERROR_START_PERIOD * Math.pow(2, attempt));
    }

    private class TrackerUpdater implements Runnable {

        private Event event;

        public TrackerUpdater(Event event) {
            this.event = event;
        }
        @Override
        public void run() {
            try {
                retrieveTrackerData(event);
            } catch (IOException | BencodeReadException e) { //TODO treat these exceptions
                long nextErrorDelay = getTrackerErrorDelay();
                logger.log(Level.INFO, Messages.TRACKER_UNREACHABLE.getText() + " - " + announceAddr + " retrying in " + nextErrorDelay + " seconds");
                scheduleTrackerUpdate(nextErrorDelay, TimeUnit.SECONDS, event);
                increaseTrackerDelay();
            }
        }

    }
}
