package main.tracker.http;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.TorrentFile;
import main.tracker.Tracker;
import main.tracker.TrackerQueryResult;
import main.util.Messages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpTracker extends Tracker{

    private Logger logger = Logger.getLogger(this.getClass().getName());

    public HttpTracker(TorrentFile torrentFile, String announceAddr) {
        super(torrentFile, announceAddr);
    }

    /**
     * Update tracker information, and retrieve torrent data
     * @param event type of event announced to tracker: STARTED, STOPPED, UNSPECIFIED
     * @throws IOException
     * @throws BencodeReadException fail when decoding string received from tracker
     */
    protected TrackerQueryResult sendTrackerAnnounce(Event event) throws IOException, BencodeReadException {
        String trackerRequest = HttpTrackerHelper.generateTrackerRequest(this.torrentFile.getTorrentId(), event, this.announceAddr);
        byte[] result = HttpTrackerHelper.sendTrackerRequest(trackerRequest);
        return new HttpTrackerQueryResult(result);
    }
}
