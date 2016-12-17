package test.tracker;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.tracker.Tracker;
import main.tracker.http.HttpTrackerHelper;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

/**
 * Created by marcelo on 19/11/16.
 */
public class TrackerHelperTest {

    private final String trackerURL = "http://127.0.0.1";

    @Test
    public void trackerRequestGenerationTest() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFile tf = TorrentManager.getInstance().addTorrent("resource/torrent/test.torrent", "resource/files/", null);
        String expectedRequest = "http://127.0.0.1/announce?port=9999&info_hash=g%ffqp%26%f2e%0c%09%b5B%a2%2b2%91%20%84%c9%ad%8f"
                + "&peer_id=" + tf.getPeerId().asURLEncodedString()
                + "&uploaded=" + tf.getUploaded() + "&downloaded=" + tf.getDownloaded() + "&left=" + tf.getLeft()
                + "&event=" + Tracker.Event.STARTED.toString().toLowerCase();

        String receivedRequest = HttpTrackerHelper.generateTrackerRequest(tf.getTorrentId(), Tracker.Event.STARTED, trackerURL);
        assertEquals(expectedRequest, receivedRequest);
    }

}