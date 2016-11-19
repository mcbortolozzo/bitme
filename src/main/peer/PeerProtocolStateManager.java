package main.peer;

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

    //TODO implement methods (possibility to do something)
}
