package test.reactor;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.peer.Peer;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import test.util.TestUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;


/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class PeerTest {

    private Client client;
    private ByteBuffer validHandshake = ByteBuffer.allocate(68);
    private TorrentFile torrentFile;

    @Before
    public void setUp() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        client = new Client(9999);
        client.run();
        char pstrlen = 19;
        validHandshake.put((byte) pstrlen);
        validHandshake.put(TorrentProtocolHelper.PROTOCOL_VERSION.getBytes(StandardCharsets.ISO_8859_1));
        for(int i = 0; i < 8; i ++) validHandshake.put((byte) 0);
        this.torrentFile = TorrentManager.getInstance().addTorrent("resource/torrent/test.torrent", "resource/files/", client.getSelector());
        validHandshake.put(this.torrentFile.getTorrentId().getBytes());
        validHandshake.put(TestUtil.PEER_ID.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void peerCreationByHandshakeTest() throws IOException {
        //send dummy handshake to bittorrent client
        Socket socket = new Socket("localhost", 9999);
        OutputStream out = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write(validHandshake.array());
        out.flush();
        //wait for reply
        StringBuilder sb = new StringBuilder();
        while (!in.ready()) ; //TODO find better method for that
        while (in.ready()) {
            char[] c = new char[]{1024};
            in.read(c);
            sb.append(c);
        }
        //validate torrent id received
        String reply = sb.toString();
        Assert.assertEquals(new String(this.torrentFile.getTorrentId().getBytes()), reply.substring(28, 47));
    }

    @Test
    public void peerIdGenerationTest() throws IOException {
        Peer peer1 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
        Peer peer2 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
        Assert.assertEquals(20, peer1.getLocalPeerId().length());
        Assert.assertEquals(20, peer2.getLocalPeerId().length());
        Assert.assertEquals(peer1.getLocalPeerId(), peer2.getLocalPeerId());
        //Assert.assertNotEquals(peer1.getLocalPeerId(), peer3.getLocalPeerId());
    }

    @Test
    public void peerSendHandshake(){
        Peer p = null;
        try {
            p = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
            p.sendHandshake(null);
            int timeout = 0;
            while(p.getOtherPeerId() == null && timeout < 10){
                timeout++;
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(p);
        Assert.assertNotNull(p.getOtherPeerId());
    }

    @Test
    public void multipleConnectionsOneTorrentTest(){
        Peer p1 = null, p2 = null;
        try {
            p1 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
            p2 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
            p1.sendHandshake(null);
            p2.sendHandshake(null);
            int timeout = 0;
            while((p1.getOtherPeerId() == null || p2.getOtherPeerId() == null) && timeout < 10){
                timeout++;
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(p1);
        Assert.assertNotNull(p2);
        Assert.assertNotNull(p1.getOtherPeerId());
        Assert.assertNotNull(p2.getOtherPeerId());
    }

    @After
    public void tearDown() throws IOException {
        this.client.shutdown();
    }
}