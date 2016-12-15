package main.torrent.file;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class FileBlockInfo {

    Logger logger = Logger.getLogger(this.getClass().getName());

    private String filePath;
    private Long localFileBegin;
    private int localBlockLength; //read or write length

    /**
     * Creates an object of this class, which identifies data to be read in a single file
     * Pieces may be divided in more than one file, and so this object identifies the data not by piece,
     * but by file. This way the read from disk may be executed for each file and the data added to a buffer
     * @param filePath the path to the file from which the data for this block will be read
     * @param localFileBegin the position from which the read will start
     * @param localBlockLength the amount of data to be read from this file (not the total length for all the read)
     */
    public FileBlockInfo(String filePath, Long localFileBegin, int localBlockLength) {
        this.filePath = filePath;
        this.localFileBegin = localFileBegin;
        this.localBlockLength = localBlockLength;
    }

    /**
     * Access the disk according to the information specified above and reads the data to a buffer
     * @return the buffer containing the data
     * @throws IOException throws exception in case of failure to read the file or buffer
     */
    public ByteBuffer readFileData() throws IOException {
        logger.log(Level.FINE, "Reading " + this.localBlockLength + " bytes to " + this.filePath + " at " + this.localFileBegin);
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

    /**
     * Write data from provided buffer to the corresponding file
     * @param outputBuffer the buffer containing the data
     * @throws IOException throws exception in case of failure to read the file or buffer
     */
    public void writeFileData(ByteBuffer outputBuffer) throws IOException {
        logger.log(Level.FINE, "Writing " + this.localBlockLength + " bytes to " + this.filePath + " at " + this.localFileBegin);
        ByteBuffer localBuffer = ByteBuffer.allocate(this.localBlockLength);
        byte[] bufferData = new byte[this.localBlockLength];    //gj java
        outputBuffer.get(bufferData);
        localBuffer.put(bufferData);
        localBuffer.flip();
        RandomAccessFile fOut = new RandomAccessFile(filePath, "rw");
        FileChannel channel = fOut.getChannel();
        channel.write(localBuffer, localFileBegin);
        channel.close();
        fOut.close();
    }

    /**
     * The length of the read/write executed by this block in one file
     * @return
     */
    public int getLength() {
        return localBlockLength;
    }
}
