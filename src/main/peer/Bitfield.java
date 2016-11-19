package main.peer;

import main.torrent.TorrentFile;

import java.nio.ByteBuffer;

/**
 * Created by marcelo on 19/11/16.
 */
public class Bitfield {

    private boolean[] bitfield;
    private int bitfieldLength;

    public Bitfield(TorrentFile torrentFile) {
        this.bitfieldLength = torrentFile.getPieceCount();
        bitfield = new boolean[bitfieldLength]; //defaults to false, so ok
    }

    public synchronized boolean checkHavePiece(int index){
        if(index < bitfieldLength) {
            return this.bitfield[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized void setHavePiece(int index){
        if(index < bitfieldLength){
            this.bitfield[index] = true;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized  void updateBitfield(ByteBuffer bitfieldBuffer){
        //TODO
        throw new UnsupportedOperationException();
    }

}
