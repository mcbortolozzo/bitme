package main;

import main.reactor.Dispatcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by marcelo on 24/10/16.
 */
public class Client {

    private static final int PORT = 9999;

    public static void main(String args[]) throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(PORT));
        server.configureBlocking(false);

        Dispatcher dispatcher = new Dispatcher(server);
        dispatcher.run();
    }
}
