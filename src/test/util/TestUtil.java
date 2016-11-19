package test.util;

import main.Client;
import main.peer.Peer;
import main.peer.PeerConnection;
import main.torrent.TorrentFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by marcelo on 19/11/16.
 */
public class TestUtil {

    public static final String TORRENT_ID = "torrentID01234567890";
    public static final String PEER_ID = "peerID01234567890123";
    public static final int PIECE_SIZE = 32000;
    public static final int PIECE_COUNT = 5000;

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
}
