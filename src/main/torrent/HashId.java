package main.torrent;

import main.util.Utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by marcelo on 19/11/16.
 */
public class HashId {

    private byte[] hashIdBytes;
    private String hashIdString;

    public HashId(byte[] hashIdBytes){
        this.hashIdBytes = hashIdBytes;
        this.hashIdString = calculateHashIdString();
    }

    public HashId(String encodedString){
        this.hashIdBytes = Utils.decodeURLString(encodedString);
        this.hashIdString = encodedString;
    }

    public byte[] getBytes(){
        return this.hashIdBytes;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof HashId){
            HashId other = (HashId) o;
            if(Arrays.equals(this.getBytes(), other.getBytes())){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.hashIdBytes);
    }

    public String toString(){
        return this.hashIdString;
    }

    public String calculateHashIdString(){
        StringBuilder builder = new StringBuilder();
        for(byte b: this.hashIdBytes){
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
    public String asURLEncodedString() throws UnsupportedEncodingException {
        return Utils.encodeToURLString(this.hashIdBytes);
    }

    public int length() {
        return this.hashIdBytes.length;
    }
}
