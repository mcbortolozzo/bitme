package tracker;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.tracker.TrackerHelper;
import org.junit.Before;
import org.junit.Test;
import util.TestUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

/**
 * Created by marcelo on 19/11/16.
 */
public class TrackerHelperTest {

    private final String trackerURL = "http://127.0.0.1";

    @Test
    public void trackerRequestGenerationTest() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFile tf = TorrentManager.getInstance().addTorrent("resource/files/test.torrent");
        String expectedRequest = "http://127.0.0.1/announce?port=9999&info_hash=%0a%92%06%a8%df%cf%aczY%aes%bdiW%92%24%02rW3"
                + "&peer_id=" + tf.getPeerId().asURLEncodedString()
                + "&uploaded=" + tf.getUploaded() + "&downloaded=" + tf.getDownloaded() + "&left=" + tf.getLeft()
                + "&event=" + TrackerHelper.Event.STARTED.toString().toLowerCase();

        String receivedRequest = TrackerHelper.generateTrackerRequest(tf.getTorrentId(), TrackerHelper.Event.STARTED, trackerURL);
        assertEquals(expectedRequest, receivedRequest);
    }

    @Test
    public void test() throws IOException, BencodeReadException, NoSuchAlgorithmException {
        TorrentFile tf = TorrentManager.getInstance().addTorrent("resource/files/test.torrent");
        tf.retrieveTrackerData(TrackerHelper.Event.STARTED);
    }
}