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

    protected ArrayList<PeerTrackerData> peerList;

    public class PeerTrackerData {
        public HashId peerId;
        public String peerIp;
        public Long peerPort;

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
            return new TrackerPeerByteDictionary((String) peers);// TODO implement binary model (find an example of it first)
    }

    public ArrayList<PeerTrackerData> getPeers(){
        return peerList;
    }
}
