package main.torrent.file;

import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.protocol.TorrentProtocolHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class BlockPieceManager {

    private static final int BLOCK_SIZE = (int) Math.pow(2,14);

    TorrentFileInfo fileInfo;
    private HashMap<Integer, ArrayList<byte[]>> downloadingPieces;
    private Bitfield bitfield;
    private ArrayList<Integer> notStartedPieces = new ArrayList<>();
    private int nbPieces;
    private Long lengthPiece;
    private Long lengthFile;
    private Long lengthLastPiece;
    private int nbBlocks;
    private int nbBlocksLastPiece;

    public BlockPieceManager(int nbPieces, Long lengthPiece, Long lengthFile, Bitfield bitfield, TorrentFileInfo fileInfo) {
        this.downloadingPieces = new HashMap<>();
        this.nbPieces = nbPieces;
        this.lengthPiece = lengthPiece;
        this.lengthFile = lengthFile;
        this.lengthLastPiece = lengthFile - (nbPieces - 1)*lengthPiece;
        this.nbBlocks = (int) Math.ceil(lengthPiece.intValue()/ BLOCK_SIZE);
        this.nbBlocksLastPiece = (int) Math.ceil(lengthLastPiece.intValue()/ BLOCK_SIZE);
        this.fileInfo = fileInfo;

        this.bitfield = bitfield;

        for (int i = bitfield.getBitfield().nextClearBit(0); i < bitfield.getBitfieldLength(); i = bitfield.getBitfield().nextClearBit(i+1)) {
            notStartedPieces.add(i);
            if (i == Integer.MAX_VALUE) {
                break;
            }
        }
    }

    public void beginDownloading(int index, Peer p) {
        if(index >= nbPieces) {
            throw new IndexOutOfBoundsException();
            //TODO add exception treatment
        }
        synchronized (this) {
            if(index == nbPieces - 1) {
                downloadingPieces.computeIfAbsent(index, k -> new ArrayList<>(nbBlocksLastPiece));
                createAndSendRequest(index, 0, lengthFile.intValue(), p);
            } else {
                downloadingPieces.computeIfAbsent(index, k -> new ArrayList<>(nbBlocks));
                createAndSendRequest(index, 0, BLOCK_SIZE, p);
            }
            notStartedPieces.remove(index);
        }
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

    public void createAndSendRequest(int index, int begin, int length, Peer p) {
        ByteBuffer message = TorrentProtocolHelper.createRequest(index, begin, length);
        p.sendMessage(message);
    }

    public Boolean receiveBlock(int index, int begin, byte[] block) throws NoSuchAlgorithmException, IOException {
        if(!downloadingPieces.containsKey(index)) {
            return false;
        }
        downloadingPieces.get(index).add((int) Math.floor(begin/ BLOCK_SIZE), block);
        if(Math.floor(begin/ BLOCK_SIZE) == getNumberBlocksFromPiece(index)) {
            return validateAndSavePiece(index);
        }
        return false;
    }

    public int getNumberBlocksFromPiece(int index) {
        if(index != nbPieces - 1) {
            return nbBlocks;
        } else {
            return nbBlocksLastPiece;
        }
    }

    public int getPieceSizeFromIndex(int index) {
        if(index != nbPieces - 1) {
            return lengthPiece.intValue();
        } else {
            return lengthLastPiece.intValue();
        }
    }

    public Boolean validateAndSavePiece(int index) throws NoSuchAlgorithmException, IOException {
        if(downloadingPieces.get(index).size() == getNumberBlocksFromPiece(index)) {
            ByteBuffer pieceBuffer = ByteBuffer.allocate(getPieceSizeFromIndex(index));
            for(byte[] block : downloadingPieces.get(index)) {
                pieceBuffer.put(block);
            }
            if(fileInfo.isPieceValid(pieceBuffer.array(), index)) {
                TorrentBlock tb = fileInfo.getFileBlock(index, 0, getPieceSizeFromIndex(index));
                pieceBuffer.flip();
                tb.writeFileBlock(pieceBuffer);
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

    public ArrayList<Integer> getNotStartedPieces() {
        return notStartedPieces;
    }
}
