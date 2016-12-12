package test.torrent.file;

import com.hypirion.bencode.BencodeReadException;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.file.MultipleFileInfo;
import main.torrent.file.TorrentBlock;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentBlockTest {

    @Test
    public void MultiFileBlockRead() throws BencodeReadException, NoSuchAlgorithmException, IOException {
        ByteBuffer expectedBuffer = ByteBuffer.allocate(257 + 257);
        expectedBuffer.put(Files.readAllBytes(Paths.get("resource/files/testFile1")));
        expectedBuffer.put(Files.readAllBytes(Paths.get("resource/files/testFile2")));

        TorrentFile tf = TorrentManager.getInstance().addTorrent("resource/torrent/mtest.torrent", "resource/files/", null);
        TorrentBlock tb = tf.getBlockInfo(0, 0, 257 + 257);
        ByteBuffer bb = tb.readFileBlock();

        assertNotNull(bb);
        assertTrue(expectedBuffer.equals(bb));
    }


}
