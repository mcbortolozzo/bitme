package test.torrent.protocol;

import com.hypirion.bencode.BencodeReadException;
import main.Client;
import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentRequest;
import main.torrent.protocol.requests.StateChangeRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.util.TestUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import static main.torrent.protocol.RequestTypes.*;
import static main.torrent.protocol.TorrentProtocolHelper.createStateChangeMessage;
import static org.junit.Assert.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class StateChangeRequestTest {

    private Client client;
    private TorrentFile torrentFile;

    @Before
    public void setUp() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        this.client = new Client(9999);
        this.torrentFile = TorrentManager.getInstance().addTorrent("resource/torrent/test.torrent", "resource/files/", client.getSelector());

    }

    @Test
    public void chokeCreationAndProcessingTest() {
        ByteBuffer chokeBuffer = createStateChangeMessage(CHOKE);
        chokeBuffer.flip();
        StateChangeRequest chokeRequest = new StateChangeRequest(chokeBuffer, CHOKE);
        assertEquals(0, chokeRequest.getMessageType());
        assertEquals(1, chokeRequest.getMessageLength());

        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(chokeRequest);
        Peer p1 = TestUtil.processRequests(requests, client, torrentFile);
        assertEquals(true, p1.isPeerChoking());
    }

    @Test
    public void unchokeCreationAndProcessingTest() {
        ByteBuffer unchokeBuffer = createStateChangeMessage(UNCHOKE);
        unchokeBuffer.flip();
        StateChangeRequest unchokeRequest = new StateChangeRequest(unchokeBuffer, UNCHOKE);
        assertEquals(1, unchokeRequest.getMessageType());
        assertEquals(1, unchokeRequest.getMessageLength());

        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(unchokeRequest);
        Peer p1 = TestUtil.processRequests(requests, client, torrentFile);
        assertEquals(false, p1.isPeerChoking());
    }

    @Test
    public void interestedCreationAndProcessingTest() {
        ByteBuffer interestedBuffer = createStateChangeMessage(INTERESTED);
        interestedBuffer.flip();
        StateChangeRequest interestedRequest = new StateChangeRequest(interestedBuffer, INTERESTED);
        assertEquals(2, interestedRequest.getMessageType());
        assertEquals(1, interestedRequest.getMessageLength());

        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(interestedRequest);
        Peer p1 = TestUtil.processRequests(requests, client, torrentFile);
        assertEquals(true, p1.isPeerInterested());
    }

    @Test
    public void notInterestedCreationAndProcessingTest() {
        ByteBuffer notInterestedBuffer = createStateChangeMessage(NOT_INTERESTED);
        notInterestedBuffer.flip();
        StateChangeRequest notInterestedRequest = new StateChangeRequest(notInterestedBuffer, NOT_INTERESTED);
        assertEquals(3, notInterestedRequest.getMessageType());
        assertEquals(1, notInterestedRequest.getMessageLength());

        List<TorrentRequest> requests = new LinkedList<>();
        requests.add(notInterestedRequest);
        Peer p1 = TestUtil.processRequests(requests, client, torrentFile);
        assertEquals(false, p1.isPeerInterested());
    }

    @After
    public void tearDown() throws IOException {
        this.client.shutdown();
    }

}
