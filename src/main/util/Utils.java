package main.util;

import main.torrent.HashId;
import main.torrent.protocol.TorrentProtocolHelper;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class Utils {

    public static final List<Byte> validURLchars = generateValidURLByteList();
    public static final char PERCENT_CHAR = '%';

    public static String encodeToURLString(byte[] bytes){
        StringBuilder urlEncoded = new StringBuilder();
        for(byte b: bytes){
            if(validURLchars.contains(b)){
                urlEncoded.append((char) b);
            } else {
                urlEncoded.append("%").append(String.format("%02x", b));
            }
        }
        return urlEncoded.toString();
    }

    public static byte[] decodeURLString(String urlData){
        byte[] result = new byte[TorrentProtocolHelper.ID_LEN];
        int stringIndex = 0;
        char current;
        for(int i = 0; i < result.length; i++){
            current = urlData.charAt(stringIndex++);
            if(current != PERCENT_CHAR){
                result[i] = (byte) current;
            } else {
                result[i] = (byte) Integer.parseInt(urlData.substring(stringIndex, stringIndex + 2), 16);
                stringIndex += 2;
            }
        }
        return result;
    }

    private static List<Byte> generateValidURLByteList() {
        byte[] validBytes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGIJKLMNOPQRSTUVWXYZ.-_~".getBytes();
        List<Byte> validList = new LinkedList<>();
        for(byte b: validBytes){
            validList.add(b);
        }
        return validList;
    }
}
