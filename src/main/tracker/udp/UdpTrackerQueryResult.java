package main.tracker.udp;

import main.tracker.TrackerPeerByteDictionary;
import main.tracker.TrackerQueryResult;

import java.nio.ByteBuffer;

public class UdpTrackerQueryResult extends TrackerQueryResult{

    public UdpTrackerQueryResult(ByteBuffer replyBuffer){
        this.interval = (long) replyBuffer.getInt();
        this.leechers = (long) replyBuffer.getInt();
        this.seeders = (long) replyBuffer.getInt();
        this.peerInfo = new TrackerPeerByteDictionary(replyBuffer);
    }

    public UdpTrackerQueryResult(String failureReason){
        this.failure = true;
        this.failureReason = failureReason;
    }

}
