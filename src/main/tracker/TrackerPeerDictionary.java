package main.tracker;

import main.torrent.HashId;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TrackerPeerDictionary extends TrackerPeerInfo {

    public TrackerPeerDictionary(ArrayList peers) {
        peerList = new ArrayList<>();
        for(Object p : peers){
            if(p instanceof HashMap){
                peerList.add(this.parsePeer((HashMap) p));
            }
        }
    }

    private PeerTrackerData parsePeer(HashMap peer) {
        Long port = (Long) peer.get("port");
        String ip = (String) peer.get("ip");
        HashId id = new HashId((String) peer.get("id"));
        return new PeerTrackerData(id, ip, port);
    }
}
