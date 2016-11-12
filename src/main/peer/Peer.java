package main.peer;

import main.Client;
import main.reactor.PeerHandler;
import main.torrent.TorrentFile;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by marcelo on 07/11/16.
 */
public class Peer{

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
    private PeerConnection peerConnection;

    private TorrentFile torrentFile;
    private String otherPeerId;
    private final String localPeerId = generatePeerId();

    public Peer(SocketChannel socket, Selector selector) throws IOException {
        this.peerConnection = new PeerConnection(socket, selector, this);
    }

    public Peer(Selector selector, TorrentFile torrentFile, SocketAddress destAddr) throws IOException {
        this.torrentFile = torrentFile;
        this.peerConnection = new PeerConnection(selector, destAddr, this);
    }

    private static String generatePeerId(){
        String peerId;
        peerId = "-" + Client.CLIENT_ID + Client.CLIENT_VERSION + "-";
        for(int i = 0; i < 12; i++){
            peerId += new SecureRandom().nextInt(10);
        }
        byte[] bytes = peerId.getBytes();
        return peerId;
    }

    public void process(List<TorrentRequest> requests){
        for(TorrentRequest req : requests){
            req.setPeer(this);
            executorService.execute(req);
        }
    }

    public void sendMessage(ByteBuffer message){
        peerConnection.addToBuffer(message);
    }

    public void sendHandshake(){
        ByteBuffer message = TorrentProtocolHelper.createHandshake(this.torrentFile.getTorrentId(), this.getLocalPeerId());
        this.sendMessage(message);
    }

    public String getLocalPeerId() {
        return localPeerId;
    }

    public void setTorrentFile(TorrentFile torrentFile) {
        this.torrentFile = torrentFile;
    }

    public void setOtherPeerId(String peerId) {
        this.otherPeerId = peerId;
    }

    public String getOtherPeerId() {
        return this.otherPeerId;
    }
}
