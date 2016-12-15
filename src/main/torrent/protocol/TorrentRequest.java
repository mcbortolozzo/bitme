package main.torrent.protocol;

import main.peer.Peer;

import java.nio.ByteBuffer;

/**
 * Created by marcelo on 07/11/16.
 */
public abstract class TorrentRequest implements ExecutableTask, Runnable {

    public static class OutboundMessage {
        public TorrentRequest request;
        public ByteBuffer buffer;
        public boolean started = false;

        public OutboundMessage(ByteBuffer buffer, TorrentRequest request) {
            this.request = request;
            this.buffer = buffer;
        }
    }

    protected Peer peer;

    @Override
    public void run() {
        this.processRequest();
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
