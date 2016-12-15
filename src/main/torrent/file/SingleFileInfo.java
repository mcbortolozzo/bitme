package main.torrent.file;

import main.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

    Logger logger = Logger.getLogger(SingleFileInfo.class.getName());

    public SingleFileInfo(){
        super();
    }

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
    public Map<String, Object> generateTorrent(List<File> file,String directoryName, String announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {
        this.information.put("length", file.get(0).length());

        return super.generateTorrent(file,directoryName, announce, comment, piece_Length);
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

    @Override
    public void verifyAndAllocateFiles() {
        Utils.generateFile(this.filesSaveFolder + '/' + this.name, this.len_file);
    }
}
