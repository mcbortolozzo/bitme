package main.torrent;

import main.peer.Bitfield;
import main.peer.Peer;
import main.torrent.file.BlockPieceManager;
import main.torrent.file.TorrentFileInfo;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.requests.PieceRequest;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class PieceSelectionAlgorithm implements Runnable {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public static final int RUN_PERIOD = 25; // in miliseconds;
    private static final int MAX_REQUESTS = 1;

    private List<Peer> peers;
    private HashMap<Integer, LinkedList<Peer>> pieceDistribution;
    private ArrayList<Integer> pieceQuantity;
    private BlockPieceManager blockPieceManager;
    private int nbPieces;

    private BitSet piecesRequested;
    private boolean pause;
    private boolean torrentFinished = false;


    public PieceSelectionAlgorithm(BlockPieceManager blockPieceManager, TorrentFileInfo fileInfo, Bitfield bitfield){
        this.blockPieceManager = blockPieceManager;
        this.pieceDistribution = new HashMap<>(fileInfo.getPieceCount());
        this.pieceQuantity = new ArrayList<>((Collections.nCopies(fileInfo.getPieceCount(), 0)));
        this.nbPieces = fileInfo.getPieceCount();
        this.piecesRequested = generatePiecesRequested(bitfield);

    }

    private BitSet generatePiecesRequested(Bitfield bitfield) {
        return (BitSet) bitfield.getBitfield().clone();
    }

    public synchronized void updatePeers(List<Peer> peers){
            this.peers = peers;
    }

    public synchronized void updateAvailablePieces(Bitfield bitfield, Peer p) {
        for (int i = 0; i < bitfield.getBitfieldLength(); i++) {
            if(p.hasPiece(i)) {
                pieceDistribution.computeIfAbsent(i, k -> new LinkedList<Peer>());
                pieceDistribution.get(i).add(p);
                pieceQuantity.set(i, pieceQuantity.get(i) + 1);
            }
        }
    }

    public synchronized void updatePiecesFromHave(int index, Peer p) {
        if(index >= p.getBitfield().getBitfieldLength()) {
            throw new IndexOutOfBoundsException();
            //TODO Treat exception
        }
        pieceDistribution.computeIfAbsent(index, k -> new LinkedList<Peer>());
        pieceDistribution.get(index).add(p);
        pieceQuantity.set(index, pieceQuantity.get(index) + 1);
    }

    public synchronized void updatePieceDistribution(HashMap<Integer, LinkedList<Peer>> pieceDistribution) {
        this.pieceDistribution = pieceDistribution;
    }

    public synchronized void updatePieceQuantity(ArrayList<Integer> pieceQuantity) {
        this.pieceQuantity = pieceQuantity;
    }

    private synchronized boolean checkEndGame() {
        return this.piecesRequested.nextClearBit(0) >= this.nbPieces;
    }

    @Override
    public synchronized void run() {
        try {
            synchronized (this) {
                if(pause || torrentFinished){
                    return;
                }
                LinkedList<Peer> peersWithPiece = new LinkedList<>();
                int result = -1;
                if(checkEndGame()){
                    this.blockPieceManager.runEndGame(this.peers);
                } else {
                    while (result != BlockPieceManager.CAP_REACHED) {
                        result = BlockPieceManager.CAP_REACHED;
                        if (!blockPieceManager.getNotStartedPieces().isEmpty()) {
                            int index = blockPieceManager.getNotStartedPieces().peek();
                            peersWithPiece = pieceDistribution.get(index);
                            if (peersWithPiece != null) {
                                int randPeer = new Random().nextInt(peersWithPiece.size());
                                if (!peersWithPiece.get(randPeer).isPeerChoking()) {
                                    this.piecesRequested.set(index);
                                    result = blockPieceManager.beginDownloading(index, peersWithPiece.get(randPeer));
                                    continue;
                                }
                            }
                        }
                    }
                }
                torrentFinished = blockPieceManager.checkTorrentEnd();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void setPause(boolean pause) {
        this.pause = pause;
    }
}
