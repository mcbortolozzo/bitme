package tracker;

import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.tracker.TrackerHelper;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

/**
 * Created by marcelo on 19/11/16.
 */
public class TrackerHelperTest {

    private final String trackerURL = "http://127.0.0.1";
    private TorrentFile tf;

    @Before
    public void setUp() throws IOException {
        TorrentManager.getInstance().addTorrent(new HashId(TestUtil.TORRENT_ID.getBytes()), 10000);
        tf = TorrentManager.getInstance().retrieveTorrent(new HashId(TestUtil.TORRENT_ID.getBytes()));
    }


    @Test
    public void trackerRequestGenerationTest() throws MalformedURLException {
        String expectedRequest = "http://127.0.0.1/announce?port=9999&info_hash=" + new HashId(TestUtil.TORRENT_ID.getBytes()).asURLEncodedString()
                + "&peer_id=" + tf.getPeerId().asURLEncodedString()
                + "&uploaded=" + tf.getUploaded() + "&downloaded=" + tf.getDownloaded() + "&left=" + tf.getLeft()
                + "&event=" + TrackerHelper.Event.STARTED.toString().toLowerCase();

        String receivedRequest = TrackerHelper.generateTrackerRequest(TestUtil.TORRENT_ID.getBytes(), TrackerHelper.Event.STARTED, trackerURL);
        assertEquals(expectedRequest, receivedRequest);
    }

}