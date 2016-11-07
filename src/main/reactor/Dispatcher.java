package main.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by marcelo on 18/10/16.
 */
public class Dispatcher implements Runnable{

    private final Selector selector;

    public Dispatcher(ServerSocketChannel server) throws IOException {
        this.selector = Selector.open();
        SelectionKey sk = server.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor(selector, server));
    }

    private void dispatch(SelectionKey key){
        Runnable r = (Runnable) key.attachment();
        if(r != null){
            r.run();
        }
    }

    @Override
    public void run() {
        System.out.println("dispatcher running");
        while(true) {
            try {
                this.selector.select();
                Set selectedKeys = this.selector.selectedKeys();
                Iterator handleIterator = selectedKeys.iterator();
                while (handleIterator.hasNext()) {
                    this.dispatch((SelectionKey) handleIterator.next());
                    handleIterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
