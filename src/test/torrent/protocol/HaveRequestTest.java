package test.torrent.protocol;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.peer.Peer;
import main.peer.PeerConnection;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.torrent.protocol.requests.HaveRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.util.TestUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class HaveRequestTest {

    private Client client;
    private TorrentFile torrentFile;

    @Before
    public void setUp() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        this.client = new Client(9999);
        this.torrentFile = TorrentManager.getInstance().addTorrent("resource/torrent/test.torrent", "resource/files/");
    }

    @Test
    public void haveCreationTest(){
        ByteBuffer haveBuffer1 = TorrentProtocolHelper.createHave(0); // piece index
        haveBuffer1.flip();
        HaveRequest have1 = new HaveRequest(haveBuffer1);
        ByteBuffer haveBuffer2 = TorrentProtocolHelper.createHave(1);
        haveBuffer2.flip();
        HaveRequest have2 = new HaveRequest(haveBuffer2);
        assertEquals(4, have1.getMessageType());
        assertEquals(4, have2.getMessageType());
        assertEquals(5, have1.getMessageLength());
        assertEquals(5, have2.getMessageLength());
        assertEquals(0, have1.getPieceIndex());
        assertEquals(1, have2.getPieceIndex());
    }

    @Test
    public void haveProcessingTest(){
        Peer p1 = null;
        int pieceIndex = 0, wrongIndex = 102;

        ByteBuffer haveBuffer =TorrentProtocolHelper.createHave(pieceIndex);
        haveBuffer.flip();
        HaveRequest haveRequest = new HaveRequest(haveBuffer);
        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(haveRequest);
        try {
            p1 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
            p1.sendHandshake();
            p1.process(requests);
            int timeout = 0;
            while(p1.getOtherPeerId() == null && timeout < 10){
                timeout++;
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        List<PeerConnection> peers = TestUtil.getPeers(this.client);
        boolean indexFound = false;
        for(PeerConnection p : peers){
            if(p.getPeer().hasPiece(pieceIndex)){
                indexFound = true;
            }
        }
        assertEquals(true, indexFound);

        indexFound = false;
        for(PeerConnection p : peers){
            if(p.getPeer().hasPiece(wrongIndex)){
                indexFound = true;
            }
        }
        assertEquals(false, indexFound);

    }

    @After
    public void tearDown() throws IOException {
        this.client.shutdown();
    }

}