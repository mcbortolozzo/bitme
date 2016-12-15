package main.util;

import main.peer.Peer;
import main.torrent.HashId;
import main.torrent.protocol.TorrentProtocolHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
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
        byte[] validBytes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGIJKLMNOPQRSTUVWXYZ-_.+!*'(),~".getBytes();
        List<Byte> validList = new LinkedList<>();
        for(byte b: validBytes){
            validList.add(b);
        }
        return validList;
    }


    public static byte[] calculateHash(byte[] toHash) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(toHash);
        return crypt.digest();
    }

    public static void generateFile(String path, Long length){
        File file = new File(path);
        if(!file.isFile()) {
            file.getParentFile().mkdirs();
            try {
                RandomAccessFile rFile = new RandomAccessFile(file.getAbsolutePath(), "rws");
                rFile.setLength(length);
                rFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte getBit(byte byteValue, int bitPosition){
        return (byte) (((byteValue & 0xff) >>> (7 - bitPosition)) & 1);
    }

    public static int getSpeedFromLog(List<Integer> log) {
        if (log.size() == 0)
            return 0;
        int speed = 0;
        if (log.size() < 60) {
            for (int i : log)
                speed += i;
            speed /= log.size();
        } else {
            int last = log.get(0), last30sec = 0, last1min = 0;
            for(int i = 0 ; i < 60 ; ++i) {
                if (i < 30) {
                    last30sec += log.get(i);
                    last1min += log.get(i);
                } else
                    last1min += log.get(i);
            }
            speed = (int)(last * 0.5 + last30sec/30 * 0.3 + last1min/60 * 0.2);
        }
        return speed;
    }

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    public static String prettySizePrint(long value) {
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if (value == 0)
            return "0 kb";
        if (value < 0)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(value >= divider){
                result = sizeFormat(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    protected static String sizeFormat(long value, long divider, String unit){
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return String.format("%.1f %s", Double.valueOf(result), unit);
    }

    public static String buildPath(List<String> pathList) {
        StringBuilder sb = new StringBuilder();
        for (String path : pathList)
            sb.append(path).append("/");
        return sb.toString();
    }

    public static String prettyTimePrint(long value) {
        long hours = value / 3600;
        long minutes = (value % 3600) / 60;
        long seconds = value % 60;

        String time = hours != 0 ? hours + "h " + minutes + "m" + seconds + "s" :
                        minutes != 0 ? minutes + "m " + seconds + "s" :
                                seconds + "s";
        return time;
    }

    public static Comparator<Peer> PeerSpeedComparator = new Comparator<Peer>() {
        @Override
        public int compare(Peer p1, Peer p2) {
            Integer p1DownSpeed = Utils.getSpeedFromLog(p1.getUDowloadLog());
            Integer p2DownSpeed = Utils.getSpeedFromLog(p2.getUDowloadLog());
            return p2DownSpeed.compareTo(p1DownSpeed);
        }
    };
}
