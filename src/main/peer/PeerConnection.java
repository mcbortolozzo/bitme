package main.peer;

import main.reactor.Dispatcher;
import main.torrent.protocol.TorrentProtocolHelper;
import main.torrent.protocol.TorrentRequest;
import main.torrent.protocol.requests.RequestRequest;
import main.util.Messages;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;
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
    private LinkedList<TorrentRequest.OutboundMessage> outputBuffer = new LinkedList<>();

    public PeerConnection(SocketChannel socket, Selector selector, Peer peer) throws IOException {
        this.socket = socket;
        this.peer = peer;
        this.configureSocket(socket, selector, SelectionKey.OP_READ);
    }

    public PeerConnection(Selector selector, SocketAddress destAddr, Peer peer) throws IOException {
        this.socket = SocketChannel.open();
        this.peer = peer;
        this.configureSocket(socket, selector, SelectionKey.OP_CONNECT);
        this.socket.connect(destAddr);
    }

    private void configureSocket(SocketChannel socket, Selector selector, int keyOp) throws IOException {
        this.socket.configureBlocking(false);
        synchronized (Dispatcher.SELECTOR_LOCK2) {
            selector.wakeup();
            synchronized (Dispatcher.SELECTOR_LOCK) {
                this.selectionKey = socket.register(selector, keyOp);
                this.selectionKey.attach(this);
            }
        }
    }

    private void read(){
        synchronized (readLock) {
            try {
                socket.read(inputBuffer);
                inputBuffer.flip();
                if(!inputBuffer.hasRemaining()) {
                    this.selectionKey.interestOps(this.selectionKey.interestOps() & ~SelectionKey.OP_READ);
                }
                while(inputBuffer.hasRemaining()) {
                    TorrentRequest request = TorrentProtocolHelper.decodeMessage(inputBuffer);
                    inputBuffer.compact();
                    inputBuffer.flip();
                    if(request != null) {
                        this.peer.process(request);
                    } else {
                        break;
                    }
                }
                inputBuffer.compact();

                if(this.outputBuffer.size() > 0)
                    this.selectionKey.interestOps(SelectionKey.OP_WRITE | this.selectionKey.interestOps());
            } catch (IOException e) {
                logger.log(Level.FINE, e.getMessage());
                logger.log(Level.FINE, Messages.SOCKET_READ_FAIL.getText());
                this.peer.shutdown();
            }
        }
    }

    private void write(){
        synchronized (writeLock) {
            try {
               if(outputBuffer.size() > 0){
                   TorrentRequest.OutboundMessage message = outputBuffer.pop();
                   message.started = true;
                   socket.write(message.buffer);
                   if(message.buffer.hasRemaining()){
                       message.buffer.compact();
                       message.buffer.flip();
                       outputBuffer.push(message);
                   }
               }
            } catch (IOException e) {
                logger.log(Level.FINE, e.getMessage());
                logger.log(Level.FINE, Messages.SOCKET_WRITE_FAIL.getText());
                this.peer.shutdown();
            }

            if(outputBuffer.size() > 0) {
                selectionKey.interestOps(SelectionKey.OP_WRITE | this.selectionKey.interestOps());
                synchronized (Dispatcher.SELECTOR_LOCK2) {
                    selectionKey.selector().wakeup();
                }
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public synchronized void addToBuffer(TorrentRequest.OutboundMessage message){
        synchronized (writeLock) {
            message.buffer.flip();
            outputBuffer.add(message);
            if(this.socket.isConnected())
                selectionKey.interestOps(SelectionKey.OP_WRITE | this.selectionKey.interestOps() & ~SelectionKey.OP_CONNECT);
            synchronized (Dispatcher.SELECTOR_LOCK2) {
                selectionKey.selector().wakeup();
            }
        }
    }

    public synchronized void cancelRequest(int index, int begin){
        synchronized (writeLock) {
            Iterator<TorrentRequest.OutboundMessage> iterator = this.outputBuffer.iterator();
            while(iterator.hasNext()){
                TorrentRequest.OutboundMessage message = iterator.next();
                if(message.request != null && message.request instanceof RequestRequest){
                    RequestRequest request = (RequestRequest) message.request;
                    if(request.getPieceIndex() == index && request.getBegin() == begin){
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void run() {
        //do stuff
        try{
            if(selectionKey.isReadable()){
                read();
            } else if (selectionKey.isWritable()) {
                if(outputBuffer.size() > 0) {
                    write();
                } else {
                    this.selectionKey.interestOps(SelectionKey.OP_READ);
                }
            } else if (selectionKey.isConnectable()){
                this.socket.finishConnect();
                logger.log(Level.INFO, "Peer connected - " + this.socket.getRemoteAddress());
                this.selectionKey.interestOps(SelectionKey.OP_READ & ~SelectionKey.OP_CONNECT);
                if(!this.outputBuffer.isEmpty()){
                    this.selectionKey.interestOps(this.selectionKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        } catch(CancelledKeyException e){
            logger.log(Level.FINE, "cancelled key exception");
            this.shutdown();
        } catch (IOException e) {
            logger.log(Level.FINE, e + "\n" + this.peer.getPeerIp() + ":" + this.peer.getPeerPort());
            this.shutdown();
        } catch (NoConnectionPendingException e) {
            logger.log(Level.FINE, "Finish connect called before connect, waiting for next run");
        }
    }

    public Peer getPeer() {
        return peer;
    }

    public synchronized void shutdown(){
        try {
            this.socket.close();
        } catch (IOException e) {
            logger.log(Level.INFO, Messages.SOCKET_CLOSE_FAIL.getText());
        }
        this.selectionKey.cancel();
    }
}
