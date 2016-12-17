package main.tracker.http;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;
import main.tracker.TrackerPeerInfo;
import main.tracker.TrackerQueryResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpTrackerQueryResult extends TrackerQueryResult {

    protected String warningMessage;

    protected Long minInterval;
    protected String trackerId;

    public HttpTrackerQueryResult(byte[] reply) throws IOException, BencodeReadException {
        InputStream in = new ByteArrayInputStream(reply);
        BencodeReader bReader = new BencodeReader(in, StandardCharsets.ISO_8859_1);

        Map<String, Object> dict = bReader.readDict();
        if(dict.containsKey("failure")){
            this.failure = true;
            this.failureReason = (String) dict.get("failure");
        } else {
            this.warningMessage = (String) dict.get("warning");
            this.interval = (Long) dict.get("interval");
            this.minInterval = (Long) dict.get("min interval");
            this.trackerId = (String) dict.get("tracker id");
            this.seeders = (Long) dict.get("complete");
            this.leechers = (Long) dict.get("incomplete");
            this.peerInfo = TrackerPeerInfo.generatePeerInfo(dict.get("peers"));
        }
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public Long getMinInterval() {
        return minInterval;
    }

    public String getTrackerId() {
        return trackerId;
    }
}
