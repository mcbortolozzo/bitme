package main.reactor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by marcelo on 18/10/16.
 */
public class Acceptor implements Runnable{

    private ServerSocketChannel serverSocket;
    private Selector selector;

    public Acceptor( Selector selector, ServerSocketChannel server) {
        this.serverSocket = server;
        this.selector = selector;
    }

    @Override
    public void run() {
        System.out.println("accepting connection");
        try {
            SocketChannel socket = this.serverSocket.accept();
            if(socket != null){
                new PeerHandler(selector, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
