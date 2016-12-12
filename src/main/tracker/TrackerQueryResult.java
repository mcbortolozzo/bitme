package main.tracker;

import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TrackerQueryResult {

    private boolean failure = false;
    private String failureReason;

    private String warningMessage;
    private Long interval;
    private Long minInterval;
    private String trackerId;

    private Long completePeers;
    private Long incompletePeers;
    private TrackerPeerInfo peerInfo;

    public TrackerQueryResult(String reply) throws IOException, BencodeReadException {
        InputStream in = new ByteArrayInputStream(reply.getBytes());
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
            this.completePeers = (Long) dict.get("complete");
            this.incompletePeers = (Long) dict.get("incomplete");
            this.peerInfo = TrackerPeerInfo.generatePeerInfo(dict.get("peers"));
        }
    }

    public boolean isFailure() {
        return failure;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public Long getInterval() {
        return interval;
    }

    public Long getMinInterval() {
        return minInterval;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public Long getCompletePeers() {
        return completePeers;
    }

    public Long getIncompletePeers() {
        return incompletePeers;
    }

    public TrackerPeerInfo getPeerInfo() {
        return peerInfo;
    }
}
