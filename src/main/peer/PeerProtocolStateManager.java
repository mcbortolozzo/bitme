package main.peer;


import main.torrent.protocol.RequestTypes;

import java.util.BitSet;

/**
 * Created by marcelo on 12/11/16.
 */
public class PeerProtocolStateManager {

    private boolean peerChoking = true;
    private boolean amChoked = true;
    private boolean peerInterested = false;
    private boolean amInterested = false;

    private boolean handshakeDone = false;

    private Bitfield localBitfield;
    private Bitfield remoteBitfield;

    public PeerProtocolStateManager(Bitfield localBitfield, Bitfield remoteBitfield) {
        this.localBitfield = localBitfield;
        this.remoteBitfield = remoteBitfield;
    }

    public synchronized void setHandshakeDone(boolean handshakeDone) {
        this.handshakeDone = handshakeDone;
    }

    public synchronized void setState(RequestTypes type) {
        switch(type) {
            case CHOKE:
                this.setPeerChoking(true);
                break;
            case UNCHOKE:
                this.setPeerChoking(false);
                break;
            case INTERESTED:
                this.setPeerInterested(true);
                break;
            case NOT_INTERESTED:
                this.setPeerInterested(false);
                break;
        }
    }

    public synchronized void setPeerChoking(boolean peerChoking) {
        this.peerChoking = peerChoking;
    }

    public synchronized void setPeerInterested(boolean peerInterested) {
        this.peerInterested = peerInterested;
    }

    public synchronized boolean isPeerChoking() {
        return peerChoking;
    }

    public synchronized boolean isPeerInterested() {
        return peerInterested;
    }

    public synchronized void setAmInterested(boolean amInterested) {
        this.amInterested = amInterested;
    }

    public synchronized boolean getAmInterested() {
        return amInterested;
    }

    public boolean updateInterested(){
        BitSet differentialBitSet = (BitSet) this.remoteBitfield.getBitfield().clone();
        differentialBitSet.andNot(this.localBitfield.getBitfield());
        int test = differentialBitSet.nextSetBit(0);
        this.setAmInterested(differentialBitSet.nextSetBit(0) != -1);
        return this.getAmInterested();
    }
    //TODO implement methods (possibility to do something)
}
