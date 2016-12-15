package main.torrent;

import main.peer.Peer;
import main.torrent.protocol.RequestTypes;
import main.util.Utils;

import java.util.Comparator;
import java.util.Iterator;
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

    @Override
    public void run() {
        List<Peer> localUnchoked = this.getUnchoked();
        List <Peer> toUnchoke = getToUnchoke();
        List <Peer> toChoke = new LinkedList<>(localUnchoked);
        toChoke.removeAll(toUnchoke);
        for(Peer p : toChoke){
            p.sendStateChange(RequestTypes.CHOKE);
        }

        List<Peer> nextUnchoked = new LinkedList(toUnchoke);
        nextUnchoked.removeAll(localUnchoked);
        for(Peer p : nextUnchoked){
            p.sendStateChange(RequestTypes.UNCHOKE);
        }

        this.setUnchoked(toUnchoke);
    }

    //TODO add optimistic unchoking here
    private List<Peer> getToUnchoke(){
        List<Peer> allPeers = this.getPeers();
        allPeers.sort(Utils.PeerSpeedComparator);

        List<Peer> toUnchoke = new LinkedList<>();
        Iterator<Peer> peerIterator = allPeers.iterator();
        int interestedPeers = 0;
        while(peerIterator.hasNext() && interestedPeers < MAX_DOWNLOADERS){
            Peer nextPeer = peerIterator.next();
            if(nextPeer.isPeerInterested()) interestedPeers++;
            toUnchoke.add(nextPeer);
        }
        return toUnchoke;
    }

    private List<Peer> getInterestedPeers() {
        return this.getPeers().stream().filter(Peer::isPeerInterested).collect(Collectors.toCollection(LinkedList::new));
    }

}
