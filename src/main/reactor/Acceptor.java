package main.reactor;

import main.peer.Peer;
import main.util.Messages;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marcelo on 18/10/16.
 */
public class Acceptor implements Runnable{

    private ServerSocketChannel serverSocket;
    private Selector selector;
    Logger logger = Logger.getLogger(Acceptor.class.getName());

    public Acceptor( Selector selector, ServerSocketChannel server) {
        this.serverSocket = server;
        this.selector = selector;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, Messages.CONNECTION_ACCEPT.getText());
        try {
            SocketChannel socket = this.serverSocket.accept();
            if(socket != null){
                new Peer(socket, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
