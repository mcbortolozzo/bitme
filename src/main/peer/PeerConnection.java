package main.peer;

import main.reactor.DecodeProtocolTask;
import main.reactor.Dispatcher;
import main.torrent.TorrentManager;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.util.Messages;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class PeerConnection implements Runnable {

    public static final int PEER_BUFFER_SIZE = (int) Math.pow(2, 18);
    Logger logger = Logger.getLogger(PeerConnection.class.getName());

    private final Object readLock = new Object(), writeLock = new Object();

    private Peer peer;
    private SocketChannel socket;
    private SelectionKey selectionKey;
    private ByteBuffer inputBuffer = ByteBuffer.allocate(PEER_BUFFER_SIZE);
    private LinkedList<ByteBuffer> outputBuffer = new LinkedList<>();

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
        synchronized (Dispatcher.SELECTOR_LOCK2) {
            selector.wakeup();
            synchronized (Dispatcher.SELECTOR_LOCK) {
                this.selectionKey = socket.register(selector, SelectionKey.OP_READ);
                this.selectionKey.attach(this);
            }
        }
    }

    private void read(){
        synchronized (readLock) {
            try {
                socket.read(inputBuffer);
                if(inputBuffer.position() > 0) {
                    inputBuffer.flip();
                    TorrentManager.executorService.execute(new DecodeProtocolTask(this.peer, inputBuffer));
                    inputBuffer.clear();
                } else {
                    if(this.outputBuffer.size() > 0)
                        this.selectionKey.interestOps(SelectionKey.OP_WRITE);
                    else
                        this.selectionKey.interestOps(0);
                }
            } catch (IOException e) {
                logger.log(Level.INFO, e.getMessage());
                logger.log(Level.INFO, Messages.SOCKET_READ_FAIL.getText());
                this.peer.shutdown();
            }
        }
    }

    private void write(){
        synchronized (writeLock) {
            try {
               if(outputBuffer.size() > 0)
                    socket.write(outputBuffer.pop());
            } catch (IOException e) {
                logger.log(Level.INFO, e.getMessage());
                logger.log(Level.INFO, Messages.SOCKET_WRITE_FAIL.getText());
                this.peer.shutdown();
            }

            if(outputBuffer.size() > 0) {
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public synchronized void addToBuffer(ByteBuffer data){
        synchronized (writeLock) {
            data.flip();
            outputBuffer.add(data);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
            selectionKey.selector().wakeup();
        }
    }

    @Override
    public void run() {
        //do stuff
        try{
            if(selectionKey.isReadable()){
                read();
            } else if (selectionKey.isWritable()) {
                if(outputBuffer.size() > 0) {
                    write();
                }
            }
        } catch(CancelledKeyException e){
            logger.log(Level.INFO, "cancelled key exception");
            this.shutdown();
        }
    }

    public Peer getPeer() {
        return peer;
    }

    public void shutdown(){
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.log(Level.INFO, Messages.SOCKET_CLOSE_FAIL.getText());
        }
        this.selectionKey.cancel();
    }
}
