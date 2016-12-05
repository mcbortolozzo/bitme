package main.reactor;

import main.peer.Peer;
import main.torrent.protocol.ExecutableTask;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class DecodeProtocolTask implements Runnable {

    private Peer peer;
    private ByteBuffer buffer;

    public DecodeProtocolTask(Peer peer, ByteBuffer buffer) {
        this.peer = peer;
        this.buffer = ByteBuffer.allocate(buffer.capacity());
        this.buffer.put(buffer);
        this.buffer.flip();
    }

    @Override
    public void run() {
        try {
            List<TorrentRequest> requests = TorrentProtocolHelper.decodeStream(this.buffer);
            this.peer.process(requests);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
