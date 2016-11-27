package main.peer;

import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.util.Messages;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marcelo on 10/11/16.
 */
public class PeerConnection implements Runnable {

    public static final int PEER_BUFFER_SIZE = 5000;
    Logger logger = Logger.getLogger(PeerConnection.class.getName());

    private final Object readLock = new Object(), writeLock = new Object();

    private Peer peer;
    private SocketChannel socket;
    private SelectionKey selectionKey;
    private ByteBuffer inputBuffer = ByteBuffer.allocate(PEER_BUFFER_SIZE);
    private ByteBuffer outputBuffer = ByteBuffer.allocate(PEER_BUFFER_SIZE);
    private boolean outputBufferWrite = true;

    public PeerConnection(SocketChannel socket, Selector selector, Peer peer) throws IOException {
        this.socket = socket;
        this.peer = peer;
        this.configureSocket(socket, selector);
    }

    public PeerConnection(Selector selector, SocketAddress destAddr, Peer peer) throws IOException {
        this.socket = SocketChannel.open();
        this.peer = peer;
        this.socket.connect(destAddr);
        this.configureSocket(socket, selector);
    }

    private void configureSocket(SocketChannel socket, Selector selector) throws IOException {
        this.socket.configureBlocking(false);
        this.selectionKey = socket.register(selector, 0);
        this.selectionKey.attach(this);
        this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    private void read(){
        synchronized (readLock) {
            try {
                socket.read(inputBuffer);
                inputBuffer.flip();
                List<TorrentRequest> requests = TorrentProtocolHelper.decodeStream(inputBuffer);
                inputBuffer.flip();
                peer.process(requests);
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(){
        synchronized (writeLock) {
            try {
                logger.log(Level.FINE, Messages.WRITE_CONNECTION + " - " + new String(outputBuffer.array(), "UTF-8"));
                if (outputBufferWrite) {
                    outputBuffer.flip();
                    outputBufferWrite = false;
                }
                socket.write(outputBuffer);
                outputBuffer.clear();
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void addToBuffer(ByteBuffer data){
        synchronized (writeLock) {
            outputBufferWrite = true;
            outputBuffer.put(data.array());
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            selectionKey.selector().wakeup();
        }
    }

    @Override
    public void run() {
        //do stuff
        if (selectionKey.isWritable()) {
            if(outputBuffer.position() != 0) {
                write();
            } else if(selectionKey.isReadable()){
                read();
            }
        }
    }

    public Peer getPeer() {
        return peer;
    }
}
