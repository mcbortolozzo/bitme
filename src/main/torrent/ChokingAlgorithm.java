package main.torrent;

import main.peer.Peer;
import main.torrent.protocol.RequestTypes;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class ChokingAlgorithm implements Runnable{

    private static final int MAX_DOWNLOADERS = 4;
    private static final int NB_OPTIMISTIC_UNCHOKED_PEERS = 1;
    public static final int RUN_PERIOD = 10000; // in miliseconds;
    public static final int OPTIMISTIC_UNCHOKE_PERIOD = 30000;

    private List<Peer> peers;
    private List<Peer> unchoked = new LinkedList<>();

    public ChokingAlgorithm(List<Peer> peers) {
        this.peers = peers;
    }

    public ChokingAlgorithm() {
        this.peers = new LinkedList<>();
    }

    public synchronized void updatePeers(List<Peer> peers){
        this.peers = peers;
    }

    private synchronized List<Peer> getPeers() {
        return peers;
    }

    private synchronized List<Peer> getUnchoked() {
        return unchoked;
    }

    private synchronized void setUnchoked(List<Peer> unchoked) {
        this.unchoked = unchoked;
    }

    //TODO implement optimistic unchoking
    @Override
    public void run() {
        List<Peer> localUnchoked = this.getUnchoked();
        List <Peer> newDownloaders = getDownloaders();
        List <Peer> toChoke = new LinkedList<>(localUnchoked);
        toChoke.removeAll(newDownloaders);
        for(Peer p : toChoke){
            p.sendStateChange(RequestTypes.CHOKE);
        }

        List<Peer> toUnchoke = new LinkedList<>(newDownloaders);
        toUnchoke.removeAll(localUnchoked);
        for(Peer p : toUnchoke){
            p.sendStateChange(RequestTypes.UNCHOKE);
        }

        this.setUnchoked(newDownloaders);
    }

    private List<Peer> getDownloaders(){
        List<Peer> interested = getInterestedPeers();

        interested.sort(PeerSpeedComparator);
        int resultSize = interested.size() > MAX_DOWNLOADERS ? MAX_DOWNLOADERS : interested.size();
        return interested.subList(0, resultSize);
    }

    private List<Peer> getInterestedPeers() {
        return this.getPeers().stream().filter(Peer::isPeerInterested).collect(Collectors.toCollection(LinkedList::new));
    }

    private static Comparator<Peer> PeerSpeedComparator = new Comparator<Peer>() {
        //TODO implement download speed calculation and then compare here
        @Override
        public int compare(Peer p1, Peer p2) {
            return 0;
        }
    };
}
