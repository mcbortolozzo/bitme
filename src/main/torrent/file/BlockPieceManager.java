package main.torrent.file;

import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.protocol.TorrentProtocolHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class BlockPieceManager {

    private static final int BLOCK_SIZE = (int) Math.pow(2,14);
    public static final int MAX_DOWNLOAD_CAP = (int) (0.75 * Math.pow(2, 15));
    private static final long REQUEST_TIMEOUT = 10000;

    public static final int CAP_REACHED = 1234;

    Logger logger = Logger.getLogger(this.getClass().getName());

    private static ScheduledExecutorService timeoutExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1);

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

    private LinkedList<BlockRequest> requestsList = new LinkedList<>();

    private class BlockRequest {
        public long blockLength;
        public int pieceIndex;
        public int blockNb;
        public Peer peer;
        private Future timeout;

        BlockRequest(int pieceIndex, int blockNb, int blockLength, Peer p){
            this.blockLength = blockLength;
            this.pieceIndex = pieceIndex;
            this.blockNb = blockNb;
            this.peer = p;
            this.timeout = timeoutExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    cancelBlockRequest(pieceIndex, blockNb);
                }
            }, REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        public void finish(){
            this.timeout.cancel(false);
        }
    }

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

    public int beginDownloading(int index, Peer p) {
        if(index >= nbPieces) {
            throw new IndexOutOfBoundsException();
            //TODO add exception treatment
        }
        synchronized (this) {
            while (getBytesBeginDownloaded(p) < MAX_DOWNLOAD_CAP) {
                downloadingPieces.computeIfAbsent(index, k -> new ArrayList<>(Collections.nCopies(getNumberBlocksFromPiece(index), null)));
                try {
                    int nextPieceBlock = getNextMissingBlock(index);
                    createAndSendRequest(index, nextPieceBlock, getBlockSize(index, nextPieceBlock), p);
                } catch (IllegalArgumentException e) { //No more blocks available for request
                    this.notStartedPieces.remove(new Integer(index));
                    return -1;
                }
            }
        }
        return CAP_REACHED;
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

    private synchronized void cancelBlockRequest(int pieceIndex, int blockNb){
        logger.log(Level.INFO, "Block timed out - piece: " + pieceIndex + " block " + blockNb);
        requestsSent.clear(pieceIndex * nbBlocks + blockNb);
        removeBlockRequest(pieceIndex, blockNb);
        if(!notStartedPieces.contains(pieceIndex)){
            notStartedPieces.add(pieceIndex);
        }
    }

    private synchronized void addBlockRequest(int pieceIndex, int blockNb, int blockSize, Peer p) {
        this.bytesBeingDownloaded += blockSize;
        this.requestsList.add(new BlockRequest(pieceIndex, blockNb, blockSize, p));
    }

    private synchronized void removeBlockRequest(int pieceIndex, int blockNb){
        Iterator<BlockRequest> iterator = requestsList.iterator();
        while(iterator.hasNext()){
            BlockRequest br = iterator.next();
            this.bytesBeingDownloaded -= br.blockLength;
            if(br.pieceIndex == pieceIndex && br.blockNb == blockNb){
                br.finish();
                iterator.remove();
            }
        }
    }

    public synchronized Long getBytesBeginDownloaded(Peer p){
        return this.bytesBeingDownloaded;
        /*Long totalBytes = 0l;
        for(BlockRequest br : requestsList){
            if(br.peer.getOtherPeerId().equals(p.getOtherPeerId())){
                totalBytes += br.blockLength;
            }
        }
        return totalBytes;*/
    }

    public void createAndSendRequest(int index, int blockNb, int length, Peer p) {
        ByteBuffer message = TorrentProtocolHelper.createRequest(index, blockNb * BLOCK_SIZE, length);
        addBlockRequest(index, blockNb, length, p);
        requestsSent.set(index * nbBlocks + blockNb);
        p.sendMessage(message);
    }

    public synchronized Boolean receiveBlock(int index, int begin, byte[] block) throws NoSuchAlgorithmException, IOException {
        if(!downloadingPieces.containsKey(index)) {
            return false;
        }
        removeBlockRequest(index, (int) Math.floor((float)begin/ BLOCK_SIZE));
        downloadingPieces.get(index).set((int) Math.floor((float)begin/ BLOCK_SIZE), block);
        if(isPieceComplete(downloadingPieces.get(index))) {
            try {
                return validateAndSavePiece(index);
            } catch(NullPointerException | IOException e){
                rollBackPiece(index);
            }
        }
        return false;
    }

    private synchronized void rollBackPiece(int index) {
        this.notStartedPieces.add(index);
        for(int i = 0; i < getNumberBlocksFromPiece(index); i++){
            this.requestsSent.clear(index * nbBlocks + i);
        }
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
