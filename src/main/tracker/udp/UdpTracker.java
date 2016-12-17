package main.tracker.udp;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.TorrentFile;
import main.tracker.Tracker;
import main.tracker.TrackerQueryResult;
import main.tracker.http.HttpTrackerHelper;

import java.io.IOException;

public class UdpTracker extends Tracker {


    public UdpTracker(TorrentFile torrentFile, String announceAddr) {
        super(torrentFile, announceAddr);
    }

    @Override
    protected TrackerQueryResult sendTrackerAnnounce(Event event) throws IOException, BencodeReadException {
        return UdpTrackerHelper.sendAnnounce(this.announceAddr, torrentFile.getTorrentId(), event);
    }
}
