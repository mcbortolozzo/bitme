package reactor;

import main.Client;
import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;


/**
 * Created by marcelo on 10/11/16.
 */
public class PeerTest {

    private Client client;
    private ByteBuffer validHandshake = ByteBuffer.allocate(68);
    private TorrentFile torrentFile;

    @Before
    public void setUp() throws IOException {
        client = new Client(9999);
        client.run();
        char pstrlen = 19;
        validHandshake.put((byte) pstrlen);
        validHandshake.put(TorrentProtocolHelper.PROTOCOL_VERSION.getBytes());
        validHandshake.put(("00000000" + TestUtil.TORRENT_ID + TestUtil.PEER_ID).getBytes());
        TorrentManager.getInstance().addTorrent(new HashId(TestUtil.TORRENT_ID.getBytes()), TestUtil.PIECE_COUNT);
        this.torrentFile = TorrentManager.getInstance().retrieveTorrent(new HashId(TestUtil.TORRENT_ID.getBytes()));
    }

    @Test
    public void peerCreationByHandshakeTest() throws IOException {
        //send dummy handshake to bittorrent client
        Socket socket = new Socket("localhost", 9999);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.write(new String(validHandshake.array(), "UTF-8"));
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
        Assert.assertEquals(TestUtil.TORRENT_ID, reply.substring(28, 48));
    }

    @Test
    public void peerIdGenerationTest() throws IOException {
        Peer peer1 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
        Peer peer2 = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
        Assert.assertEquals(20, peer1.getLocalPeerId().length());
        Assert.assertEquals(20, peer2.getLocalPeerId().length());
        Assert.assertEquals(peer1.getLocalPeerId(), peer2.getLocalPeerId());

        TorrentManager.getInstance().addTorrent(new HashId("ABC123ABC--ABC123ABC".getBytes()), 2000);
        TorrentFile otherTorrent = TorrentManager.getInstance().retrieveTorrent(new HashId("ABC123ABC--ABC123ABC".getBytes()));
        Peer peer3 = TestUtil.generatePeer(this.client, otherTorrent, new InetSocketAddress("localhost", 9999));
        //Assert.assertNotEquals(peer1.getLocalPeerId(), peer3.getLocalPeerId());
    }

    @Test
    public void peerSendHandshake(){
        Peer p = null;
        try {
            p = TestUtil.generatePeer(this.client, this.torrentFile, new InetSocketAddress("localhost", 9999));
            p.sendHandshake();
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
            p1.sendHandshake();
            p2.sendHandshake();
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