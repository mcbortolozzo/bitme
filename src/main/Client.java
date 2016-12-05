package main;


import com.hypirion.bencode.BencodeReadException;
import main.reactor.Dispatcher;
import main.torrent.TorrentManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aadhi
 * Thibault Tourailles
 */
public class Client implements Runnable {

    Logger logger = Logger.getLogger(Client.class.getName());

    public static final int PORT = 6882;
    public static final String CLIENT_ID = "ET";
    public static final String CLIENT_VERSION = "0000";
    private Dispatcher dispatcher;
    private ServerSocketChannel server;

    public Client(int port) throws IOException{
        logger.log(Level.CONFIG, "client_id: " + CLIENT_ID + " client version: " + CLIENT_VERSION + " port: " + port);
        this.server = ServerSocketChannel.open();
        this.server.socket().bind(new InetSocketAddress(port));
        this.server.configureBlocking(false);
        this.dispatcher = new Dispatcher(server);
    }

    public Selector getSelector(){ return this.dispatcher.getSelector(); }

    @Override
    public void run() { //not sure if this is what we should do
        new Thread(dispatcher).start();
    }

    public void shutdown() throws IOException {
        this.server.close();
    }

    public static void main(String args[]) throws IOException, BencodeReadException, NoSuchAlgorithmException {
        Client client = new Client(PORT);
        client.run();
        TorrentManager.getInstance().addTorrent("/home/marcelo/Documents/multipiece.torrent", "/home/marcelo/Downloads");
    }
}
