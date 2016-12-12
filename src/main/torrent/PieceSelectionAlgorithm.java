package main.torrent;

import main.peer.Peer;
import main.torrent.file.BlockPieceManager;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.requests.PieceRequest;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class PieceSelectionAlgorithm implements Runnable {

    private static final int maxRequests = 10;

    private List<Peer> peers;
    private HashMap<Integer, LinkedList<Peer>> pieceDistribution;
    private ArrayList<Integer> pieceQuantity;
    private BlockPieceManager blockPieceManager;

    public PieceSelectionAlgorithm(BlockPieceManager blockPieceManager){
        this.blockPieceManager = blockPieceManager;
    }

    public synchronized void updatePeers(List<Peer> peers){
        this.peers = peers;
    }

    public synchronized void updatePieceDistribution(HashMap<Integer, LinkedList<Peer>> pieceDistribution) {
        this.pieceDistribution = pieceDistribution;
    }

    public synchronized void updatePieceQuantity(ArrayList<Integer> pieceQuantity) {
        this.pieceQuantity = pieceQuantity;
    }

    @Override
    public void run() {
        synchronized (this) {
            LinkedList<Peer> peersWithPiece;
            if(blockPieceManager.getDownloadingPieces().isEmpty()) {
                int index = blockPieceManager.getNotStartedPieces().get(0);
                peersWithPiece = pieceDistribution.get(index);
                for (Peer p : peersWithPiece) {
                    if (!p.isPeerChoking()) {
                        blockPieceManager.beginDownloading(index, p);
                        break;
                    }
                }
            } else {
                for(int i: blockPieceManager.getDownloadingPieces().keySet()) {
                    peersWithPiece = pieceDistribution.get(i);
                    for (Peer p : peersWithPiece) {
                        if (!p.isPeerChoking()) {
                            blockPieceManager.continueDownloading(i, p);
                            break;
                        }
                    }
                }
            }
        }
    }
}
