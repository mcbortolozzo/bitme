package main.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by marcelo on 18/10/16.
 */
public class PeerHandler{
    private final SelectionKey selectionKey;
    private final Selector selector;
    private final SocketChannel socket;
    private ByteBuffer inputBuffer = ByteBuffer.allocate(5000);
    private ByteBuffer outputBuffer = ByteBuffer.allocate(5000);

    public PeerHandler(Selector selector, SocketChannel socket) throws IOException {
        this.socket = socket;
        this.selector = selector;
        socket.configureBlocking(false);
        selectionKey = socket.register(selector, 0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        selector.wakeup();
    }


}
