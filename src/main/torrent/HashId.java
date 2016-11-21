package main.torrent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * Created by marcelo on 19/11/16.
 */
public class HashId {

    private byte[] hashIdBytes;

    public HashId(byte[] hashIdBytes){
        this.hashIdBytes = hashIdBytes;
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
        try {
            return new String(this.hashIdBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String asURLEncodedString() {
        StringBuilder urlEncoded = new StringBuilder();
        for(byte b : this.hashIdBytes){
            urlEncoded.append("%").append(String.format("%02X", b));
        }
        return urlEncoded.toString();
    }

    public int length() {
        return this.hashIdBytes.length;
    }
}
