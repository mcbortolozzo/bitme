package test.util;

import main.Client;
import main.peer.Peer;
import main.peer.PeerConnection;
import main.torrent.TorrentFile;
import main.torrent.protocol.TorrentRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TestUtil {

    public static final String TORRENT_ID = "torrentID01234567890";
    public static final String PEER_ID = "peerID01234567890123";
    public static final int PIECE_SIZE = 32000;
    public static final int PIECE_COUNT = 2000;

    public static Peer generatePeer(Client client, TorrentFile t, InetSocketAddress addr) throws IOException {
        Selector s = client.getSelector();
        return new Peer(s, t, addr);
    }

    public static List<PeerConnection> getPeers(Client client) {
        Selector s = client.getSelector();
        Iterator<SelectionKey> keys = s.keys().iterator();
        List<PeerConnection> peers = new LinkedList<>();
        while(keys.hasNext()){
            SelectionKey k = keys.next();
            if(k.attachment() instanceof PeerConnection){
                peers.add((PeerConnection) k.attachment());
            }
        }
        return peers;
    }

    public static Peer processRequests(List<TorrentRequest> requests, Client client, TorrentFile torrentFile) {

        Peer p1 = null;

        try {
            p1 = TestUtil.generatePeer(client, torrentFile, new InetSocketAddress("localhost", 9999));
            p1.sendHandshake(null);
            for(TorrentRequest req: requests)
                p1.process(req);
            int timeout = 0;
            while(p1.getOtherPeerId() == null && timeout < 10){
                timeout++;
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return p1;
    }
}
