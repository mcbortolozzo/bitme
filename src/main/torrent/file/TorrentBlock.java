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

    /**
     * Adds a new chunk of data to the list, containing the file in which this data
     * is contained, the position in which to start reading and how much is read from the file
     * @param nextBlock the object containing this information
     */
    public void addNextBlock(FileBlockInfo nextBlock){
        this.blockList.add(nextBlock);
    }

    /**
     * reads all the data from the blocks defined in the list into a byte buffer
     * @return the buffer containing the data from the files
     * @throws IOException in case of failure to read the file
     */
    public ByteBuffer readFileBlock() throws IOException {
        ByteBuffer blockBuffer = ByteBuffer.allocate(blockLength);
        for(FileBlockInfo blockInfo : this.blockList){
            blockBuffer.put(blockInfo.readFileData());
        }
        return blockBuffer;
    }

}
