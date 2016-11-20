package main.reactor;

import main.util.Messages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by marcelo on 18/10/16.
 */
public class Dispatcher implements Runnable{

    protected Selector selector;
    Logger logger = Logger.getLogger(Dispatcher.class.getName());

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

    public Selector getSelector(){
        return this.selector;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, Messages.DISPATCHER_RUN.getText());
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
