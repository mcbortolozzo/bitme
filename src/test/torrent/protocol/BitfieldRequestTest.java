package test.torrent.protocol;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.torrent.protocol.requests.BitfieldRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.util.TestUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static main.torrent.protocol.TorrentProtocolHelper.createBitfield;
import static org.junit.Assert.assertEquals;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class BitfieldRequestTest {

    private Client client;
    private TorrentFile torrentFile;
    private Bitfield bitfield;

    @Before
    public void setUp() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        this.client = new Client(9999);
        TorrentManager.getInstance().addTorrent("resource/torrent/test.torrent", "resource/files/", client.getSelector());
        this.torrentFile = TorrentManager.getInstance().retrieveTorrent(new HashId(TestUtil.TORRENT_ID.getBytes()));
        this.bitfield = new Bitfield(this.torrentFile);
    }

    @Test
    public void bitfieldCreationTest() {
        ByteBuffer bitfieldBuffer = createBitfield(bitfield);
        bitfieldBuffer.flip();
        BitfieldRequest bitfieldRequest = new BitfieldRequest(bitfieldBuffer);
        assertEquals(5, bitfieldRequest.getMessageType());
        assertEquals(TorrentProtocolHelper.BITFIELD_INITIAL_LENGHT + 1,
                bitfieldRequest.getMessageLength());
    }

    @Test
    public void bitfieldProcessingTest() {
        Random rnd = new Random();
        byte[] randomBytes = new byte[1];
        rnd.nextBytes(randomBytes);
        BitSet bitset = BitSet.valueOf(randomBytes);

        this.bitfield.updateBitfield(bitset);

        ByteBuffer bitfieldBuffer = createBitfield(bitfield);
        bitfieldBuffer.flip();
        BitfieldRequest bitfieldRequest = new BitfieldRequest(bitfieldBuffer);

        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(bitfieldRequest);
        Peer p1 = TestUtil.processRequests(requests, client, torrentFile);

        assertEquals(1,
                p1.getBitfield().getBitfieldLength());
        assertEquals(bitset, p1.getBitfield().getBitfield());
    }

    @After
    public void tearDown() throws IOException {
        this.client.shutdown();
    }
}
