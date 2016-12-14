package main.tracker;

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
public class TrackerPeerByteDictionary extends TrackerPeerInfo{

    public TrackerPeerByteDictionary(String peersString){
        this.peerList = new ArrayList<>();
        byte[] bytes = peersString.getBytes(StandardCharsets.ISO_8859_1);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        while(buffer.hasRemaining()){
            byte[] ip = new byte[4];
            byte[] port = new byte[2];
            buffer.get(ip);
            String ipString = getIp(ip);
            buffer.get(port);
            int portInt = getPort(port);
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

    private int getPort(byte[] portBytes){
        int portResult = ((portBytes[0] & 0xFF) << 8)
                            | (portBytes[1] & 0xFF);
        return portResult;
    }
}
