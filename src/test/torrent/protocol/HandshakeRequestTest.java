package test.torrent.protocol;

import main.torrent.HashId;
import main.torrent.protocol.TorrentProtocolHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import static com.sun.xml.internal.ws.dump.LoggingDumpTube.Position.Before;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class HandshakeRequestTest {

    private static final String INVALID_TORRENT_ID = "dummy_hash";
    private static final String VALID_TORRENT_ID = "dummy_hash0123456789";
    private static final String INVALID_PEER_ID = "peerID";
    private static final String VALID_PEER_ID = "peerID01234567890123";
    private String validHandshake = "";

    @Before
    public void setUp(){
        char pstrlen = 19;
        validHandshake += pstrlen;
        validHandshake += TorrentProtocolHelper.PROTOCOL_VERSION + "00000000" + VALID_TORRENT_ID + VALID_PEER_ID;
    }

    @Test(expected = InvalidParameterException.class)
    public void invalidTorrentIdHandshakeTest() {
        TorrentProtocolHelper.createHandshake(new HashId(INVALID_TORRENT_ID.getBytes()), new HashId(VALID_PEER_ID.getBytes()));
    }

    @Test(expected = InvalidParameterException.class)
    public void invalidPeerIdHandshakeTest() {
        TorrentProtocolHelper.createHandshake(new HashId(VALID_TORRENT_ID.getBytes()), new HashId(INVALID_PEER_ID.getBytes()));
    }

    @Test
    public void handshakeCreationTest(){
        ByteBuffer handshake = TorrentProtocolHelper.createHandshake(new HashId(VALID_TORRENT_ID.getBytes()), new HashId(VALID_PEER_ID.getBytes()));
        Assert.assertNotNull(handshake);
        try {
            Assert.assertEquals(validHandshake, new String(handshake.array(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("conversion from buffer to string failed");
            System.exit(-1);
        }
    }
}