package main.peer;

import main.torrent.TorrentFile;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Created by marcelo on 19/11/16.
 */
public class Bitfield {

    private BitSet bitfield;
    private int bitfieldLength;

    public Bitfield(TorrentFile torrentFile) {
        this.bitfieldLength = torrentFile.getPieceCount();
        bitfield = new BitSet(bitfieldLength); //defaults to false, so ok
    }

    public synchronized boolean checkHavePiece(int index){
        if(index < bitfieldLength) {
            return this.bitfield.get(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized void setHavePiece(int index){
        if(index < bitfieldLength){
            this.bitfield.set(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public synchronized void updateBitfield(BitSet bitfield){
        if(bitfield.length() <= bitfieldLength) {
            this.bitfield = bitfield;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public BitSet getBitfield() {
        return bitfield;
    }

    public int getBitfieldLength() {
        return bitfieldLength;
    }
}
