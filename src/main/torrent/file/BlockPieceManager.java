package main.torrent.file;

import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.protocol.TorrentProtocolHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class BlockPieceManager {

    private static final int BLOCK_SIZE = (int) Math.pow(2,14);
    public static final int MAX_DOWNLOAD_CAP = (int) (0.75 * Math.pow(2, 18));

    TorrentFileInfo fileInfo;
    private HashMap<Integer, ArrayList<byte[]>> downloadingPieces;
    private Bitfield bitfield;
    private LinkedList<Integer> notStartedPieces = new LinkedList<>();
    private int nbPieces;
    private Long lengthPiece;
    private Long lengthFile;
    private Long lengthLastPiece;
    private int nbBlocks;
    private int nbBlocksLastPiece;

    private BitSet requestsSent;

    private Long bytesBeingDownloaded = 0l;

    public BlockPieceManager(int nbPieces, Long lengthPiece, Long lengthFile, Bitfield bitfield, TorrentFileInfo fileInfo) {
        this.downloadingPieces = new HashMap<>();
        this.nbPieces = nbPieces;
        this.lengthPiece = lengthPiece;
        this.lengthFile = lengthFile;
        this.lengthLastPiece = lengthFile - (nbPieces - 1)*lengthPiece;
        this.nbBlocks = (int) Math.ceil(lengthPiece.floatValue()/ BLOCK_SIZE);
        this.nbBlocksLastPiece = (int) Math.ceil(lengthLastPiece.floatValue()/ BLOCK_SIZE);
        this.fileInfo = fileInfo;
        this.bitfield = bitfield;

        this.requestsSent = new BitSet(getTotalNbBlocks());
        for (int i = bitfield.getBitfield().nextClearBit(0); i < bitfield.getBitfieldLength(); i = bitfield.getBitfield().nextClearBit(i+1)) {
            notStartedPieces.add(i);
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }
    }

    private int getTotalNbBlocks() {
        return (nbPieces - 1) * nbBlocks + nbBlocksLastPiece;
    }

    public void beginDownloading(int index, Peer p) {
        if(index >= nbPieces) {
            throw new IndexOutOfBoundsException();
            //TODO add exception treatment
        }
        synchronized (this) {
            while (getBytesBeginDownloaded() < MAX_DOWNLOAD_CAP) {
                downloadingPieces.computeIfAbsent(index, k -> new ArrayList<>(Collections.nCopies(getNumberBlocksFromPiece(index), null)));
                try {
                    int nextPieceBlock = getNextMissingBlock(index);
                    createAndSendRequest(index, nextPieceBlock, getBlockSize(index, nextPieceBlock), p);
                } catch (IllegalArgumentException e) { //No more blocks available for request
                    this.notStartedPieces.remove(new Integer(index));
                    return;
                }
            }
        }
        /*
        if(index == nbPieces - 1) {

            createAndSendAllRequest(index, p);
            //createAndSendRequest(index, 0, lengthFile.intValue(), p);
        } else {
            downloadingPieces.computeIfAbsent(index, k -> new ArrayList<>(Collections.nCopies(nbBlocks, null)));
            createAndSendAllRequest(index, p);
            //createAndSendRequest(index, 0, BLOCK_SIZE, p);
        }
        */
    }

    private int getNextMissingBlock(int index) {
        int pieceBeginIndex = index * nbBlocks;
        int nextBlock = requestsSent.nextClearBit(pieceBeginIndex);
        if(nextBlock < pieceBeginIndex + nbBlocks){
            return nextBlock - pieceBeginIndex;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void createAndSendAllRequest(int index, Peer p) {
        for(int i = 0; i < getNumberBlocksFromPiece(index); i++) {
            int blockSize = getBlockSize(index, i);
            createAndSendRequest(index, i * BLOCK_SIZE, blockSize , p);
            increaseBytesDownloaded(blockSize);
            requestsSent.set(index * nbBlocks + i);
        }
    }

    private synchronized void increaseBytesDownloaded(int blockSize) {
        this.bytesBeingDownloaded += blockSize;
    }

    private synchronized void decreaseBytesDownloaded(int blockSize){
        this.bytesBeingDownloaded -= blockSize;
    }

    public synchronized Long getBytesBeginDownloaded(){
        return bytesBeingDownloaded;
    }

    public void continueDownloading(int index, Peer p) {
        if(index >= nbPieces) {
            throw new IndexOutOfBoundsException();
            //TODO add exception treatment
        }
        synchronized (this) {
            int indexBlock = downloadingPieces.get(index).indexOf(null);
            int blockOffset = indexBlock * BLOCK_SIZE;
            if(index != nbPieces - 1) {
                if(indexBlock != nbBlocks - 1) {
                    createAndSendRequest(index, blockOffset, BLOCK_SIZE, p);
                } else {
                    createAndSendRequest(index, blockOffset, lengthPiece.intValue() - BLOCK_SIZE * (nbBlocks - 1), p);
                }
            } else {
                if(indexBlock != nbBlocksLastPiece - 1) {
                    createAndSendRequest(index, blockOffset, BLOCK_SIZE, p);
                } else {
                    createAndSendRequest(index, blockOffset, lengthLastPiece.intValue() - BLOCK_SIZE * (nbBlocksLastPiece - 1), p);
                }
            }
        }
    }

    public void createAndSendRequest(int index, int blockNb, int length, Peer p) {
        ByteBuffer message = TorrentProtocolHelper.createRequest(index, blockNb * BLOCK_SIZE, length);
        increaseBytesDownloaded(length);
        requestsSent.set(index * nbBlocks + blockNb);
        p.sendMessage(message);
    }

    public Boolean receiveBlock(int index, int begin, byte[] block) throws NoSuchAlgorithmException, IOException {
        if(!downloadingPieces.containsKey(index)) {
            return false;
        }
        decreaseBytesDownloaded(block.length);
        downloadingPieces.get(index).set((int) Math.floor((float)begin/ BLOCK_SIZE), block);
        if(isPieceComplete(downloadingPieces.get(index))) {
            return validateAndSavePiece(index);
        }
        return false;
    }

    private boolean isPieceComplete(ArrayList pieceBlocks){
        for(Object bytes: pieceBlocks){
            if(bytes == null)
                return false;
        }
        return true;
    }

    private int getNumberBlocksFromPiece(int index) {
        if(index != nbPieces - 1) {
            return nbBlocks;
        } else {
            return nbBlocksLastPiece;
        }
    }

    private int getPieceSizeFromIndex(int index) {
        if(index != nbPieces - 1) {
            return lengthPiece.intValue();
        } else {
            return lengthLastPiece.intValue();
        }
    }

    private int getBlockSize(int pieceIndex, int blockNb){
        int pieceSize = getPieceSizeFromIndex(pieceIndex);
        if(blockNb == getNumberBlocksFromPiece(pieceIndex) - 1 && pieceSize % BLOCK_SIZE != 0 ){
            return pieceSize % BLOCK_SIZE;
        }
        return BLOCK_SIZE;
    }

    private Boolean validateAndSavePiece(int index) throws NoSuchAlgorithmException, IOException {
        if(downloadingPieces.get(index).size() == getNumberBlocksFromPiece(index)) {
            ByteBuffer pieceBuffer = ByteBuffer.allocate(getPieceSizeFromIndex(index));
            for(byte[] block : downloadingPieces.get(index)) {
                pieceBuffer.put(block);
            }
            if(fileInfo.isPieceValid(pieceBuffer.array(), index)) {
                TorrentBlock tb = fileInfo.getFileBlock(index, 0, getPieceSizeFromIndex(index));
                pieceBuffer.flip();
                tb.writeFileBlock(pieceBuffer);
                this.downloadingPieces.remove(index);
                return true;
            } else {
                downloadingPieces.remove(index);
                notStartedPieces.add(index);
            }
        }
        return false;
    }

    public HashMap<Integer, ArrayList<byte[]>> getDownloadingPieces() {
        return downloadingPieces;
    }

    public LinkedList<Integer> getNotStartedPieces() {
        return notStartedPieces;
    }
}
