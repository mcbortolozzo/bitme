package main.peer;


import main.torrent.protocol.RequestTypes;

/**
 * Created by marcelo on 12/11/16.
 */
public class PeerProtocolStateManager {

    private boolean peerChoking = true;
    private boolean amChoked = true;
    private boolean peerInterested = false;
    private boolean amInterested = false;

    private boolean handshakeDone = false;

    public void setHandshakeDone(boolean handshakeDone) {
        this.handshakeDone = handshakeDone;
    }

    public void setState(RequestTypes type) {
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

    public void setPeerChoking(boolean peerChoking) {
        this.peerChoking = peerChoking;
    }

    public void setPeerInterested(boolean peerInterested) {
        this.peerInterested = peerInterested;
    }

    public boolean isPeerChoking() {
        return peerChoking;
    }

    public boolean isPeerInterested() {
        return peerInterested;
    }

    //TODO implement methods (possibility to do something)
}
