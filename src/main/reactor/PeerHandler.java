package main.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by marcelo on 18/10/16.
 */
public class PeerHandler implements Runnable{
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

    private void read(){
        System.out.println("read");
        try {
            socket.read(inputBuffer);
            String data = new String(inputBuffer.array(), "ASCII");
            //TODO handle data
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(){
        try {
            System.out.println("write");
            socket.write(outputBuffer);
            outputBuffer.clear();
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            selectionKey.selector().wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addToBuffer(String data){
        outputBuffer.put(data.getBytes());
        selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        selectionKey.selector().wakeup();
    }

    @Override
    public void run() {
        //do stuff
        if (selectionKey.isReadable()) {
            read();
        } else if (selectionKey.isWritable()) {
            if(outputBuffer.position() != 0) {
                write();
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }
}
