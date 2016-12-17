package main.tracker;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TrackerQueryResult {

    protected boolean failure = false;
    protected String failureReason;

    protected Long interval;

    protected Long seeders;
    protected Long leechers;
    protected TrackerPeerInfo peerInfo;

    public boolean isFailure() {
        return failure;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Long getInterval() {
        return interval;
    }

    public Long getSeeders() {
        return seeders;
    }

    public Long getLeechers() {
        return leechers;
    }

    public TrackerPeerInfo getPeerInfo() {
        return peerInfo;
    }
}
