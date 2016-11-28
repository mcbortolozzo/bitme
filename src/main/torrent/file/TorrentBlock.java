package main.torrent.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentBlock {

    private List<FileBlockInfo> blockList;
    private int blockIndex;
    private int blockBegin;
    private int blockLength;

    public TorrentBlock(int blockIndex, int blockBegin, int blockLength) {
        this.blockList = new LinkedList<>();
        this.blockIndex = blockIndex;
        this.blockBegin = blockBegin;
        this.blockLength = blockLength;
    }

    public void addNextBlock(FileBlockInfo nextBlock){
        this.blockList.add(nextBlock);
    }

    public ByteBuffer readFileBlock() throws IOException {
        ByteBuffer blockBuffer = ByteBuffer.allocate(blockLength);
        for(FileBlockInfo blockInfo : this.blockList){
            blockBuffer.put(blockInfo.readFileData());
        }
        return blockBuffer;
    }
}
