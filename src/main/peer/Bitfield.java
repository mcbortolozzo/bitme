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

    public Bitfield(int pieceCount) {
        this.bitfieldLength = pieceCount;
        bitfield = new BitSet(bitfieldLength);
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
    //TODO check if exception not needed here
    public synchronized void updateBitfield(BitSet bitfield){
        this.bitfield = new BitSet(this.bitfieldLength);
        this.bitfield.or(bitfield);
    }

    public BitSet getBitfield() {
        return bitfield;
    }

    public int getBitfieldLength() {
        return bitfieldLength;
    }

    public byte[] getBytes(){
        byte[] bytes = new byte[(int) Math.ceil(this.bitfieldLength/8f)];
        int setBit = 0;
        while((setBit = this.bitfield.nextSetBit(setBit)) != -1){
            bytes[(int) Math.floor(setBit/8f)] |= 1 << (7 - (setBit % 8));
            setBit++;
        }
        return bytes;
    }


    public int getBitfieldLengthInBytes() {
        return (int) Math.ceil(this.getBitfieldLength()/8.0);
    }
}
