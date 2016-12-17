package main.tracker;

import main.tracker.TrackerPeerInfo;
import main.util.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TrackerPeerByteDictionary extends TrackerPeerInfo {

    public TrackerPeerByteDictionary(String peersString){
        byte[] bytes = peersString.getBytes(StandardCharsets.ISO_8859_1);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        parsePeersFromBuffer(buffer);
    }

    public TrackerPeerByteDictionary(ByteBuffer replyBuffer) {
        parsePeersFromBuffer(replyBuffer);
    }

    private void parsePeersFromBuffer(ByteBuffer buffer) {
        this.peerList = new ArrayList<>();
        while(buffer.hasRemaining() && buffer.remaining() >= 6){
            byte[] ip = new byte[4];
            byte[] port = new byte[2];
            buffer.get(ip);
            String ipString = getIp(ip);
            buffer.get(port);
            int portInt = Utils.parse2ByteInt(port);
            this.peerList.add(new PeerTrackerData(ipString, (long) portInt));
        }
    }

    private String getIp(byte[] ipBytes){
        String ipString = "";
        for(int i = 0; i < 4; i++){
            int ipPart = ipBytes[i];
            ipString += ipPart & 0xFF;
            if(i < 3)
                ipString += ".";
        }
        return ipString;
    }

}
