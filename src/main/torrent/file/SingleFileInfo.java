package main.torrent.file;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class SingleFileInfo extends TorrentFileInfo {
    private Long len_file;
    private String md5sum;

    public SingleFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        super(dict, saveFolder);

        this.len_file = (Long) this.info.get("length");
        if (this.info.containsKey("md5sum")){
            this.md5sum = (String) this.info.get("md5sum");
        }
    }

    @Override
    protected void prepareInfoField() {
        // should work properly already, no need to do anything
    }

    @Override
    public Map<String, Object> generateTorrent() throws NoSuchAlgorithmException {
        Map<String, Object> torrent = super.generateTorrent();
        torrent.put("length", this.len_file);
        torrent.put("md5sum", this.md5sum);
        return torrent;
    }

    public TorrentBlock getFileBlock(int index, int begin, int length) {
        int blockSize = this.getValidReadLength(index, begin, length);
        TorrentBlock block = new TorrentBlock(index, begin, blockSize);
        Long blockBegin = index * this.pieceSize + begin;
        block.addNextBlock(new FileBlockInfo(this.filesSaveFolder + '/' + this.name, blockBegin, blockSize));
        return block;
    }

    @Override
    public Long getLength() {
        return this.len_file;

    }
}
