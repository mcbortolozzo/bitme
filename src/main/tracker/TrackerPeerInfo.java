package main.tracker;

import main.torrent.HashId;

import java.util.ArrayList;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public abstract class TrackerPeerInfo {

    protected class PeerTrackerData {
        private HashId peerId;
        private String peerIp;
        private Long peerPort;

        public PeerTrackerData(HashId peerId, String peerIp, Long peerPort) {
            this.peerId = peerId;
            this.peerIp = peerIp;
            this.peerPort = peerPort;
        }

        public PeerTrackerData(String peerIp, Long peerPort) {
            this.peerIp = peerIp;
            this.peerPort = peerPort;
        }
    }

    public static TrackerPeerInfo generatePeerInfo(Object peers) {
        if(peers instanceof ArrayList)
            return new TrackerPeerDictionary((ArrayList) peers);
        else
            return null;// TODO implement binary model (find an example of it first)
    }
}
