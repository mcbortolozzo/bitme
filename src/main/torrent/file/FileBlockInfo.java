package main.torrent.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class FileBlockInfo {
    private String filePath;
    private Long localFileBegin;
    private int localBlockLength; //read or write length

    public FileBlockInfo(String filePath, Long localFileBegin, int localBlockLength) {
        this.filePath = filePath;
        this.localFileBegin = localFileBegin;
        this.localBlockLength = localBlockLength;
    }

    public ByteBuffer readFileData() throws IOException {
        ByteBuffer localBuffer = ByteBuffer.allocate(localBlockLength);
        FileInputStream fIn = new FileInputStream(filePath);
        FileChannel channel = fIn.getChannel();
        channel.position(localFileBegin);
        channel.read(localBuffer);
        channel.close();
        fIn.close();
        localBuffer.flip();
        return localBuffer;
    }

    public int getLength() {
        return localBlockLength;
    }
}
