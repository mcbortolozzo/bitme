package main.tracker.udp;

import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.tracker.Tracker;
import main.tracker.TrackerQueryResult;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;

public class UdpTrackerHelper {

    private static final int UDP_TIMEOUT = 5000; // in milliseconds
    private static final Long CONNECTION_ID = 0x41727101980l;
    private static final int CONNECT_ACTION = 0;
    private static final int CONNECTION_REQ_LEN = 16; //in bytes
    private static final int CONNECTION_RESP_MIN_LEN = 16;
    private static final int ANNOUNCE_ACTION = 1;
    private static final int ANNOUNCE_REQ_LEN = 98;
    private static final int ANNOUNCE_RESP_MIN_LEN = 20;

    public static Long obtainConnectionId(DatagramSocket udpSocket) throws IOException {
        int transactionId = generateTransactionId();
        sendRequest(generateConnectionRequest(transactionId), udpSocket);
        try {
            return parseConnectionReply(udpSocket, transactionId);
        } catch (InvalidUdpReplyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TrackerQueryResult sendAnnounce(String announceAddr, HashId torrentId, Tracker.Event event) throws IOException {
        DatagramSocket udpSocket = connectToTracker(announceAddr);
        Long connId = obtainConnectionId(udpSocket);
        TorrentFile torrentFile = TorrentManager.getInstance().retrieveTorrent(torrentId);
        int transactionId = generateTransactionId();
        sendRequest(generateAnnounceRequest(torrentFile, connId, transactionId, event), udpSocket);
        try {
            return parseAnnounceReply(udpSocket, transactionId);
        } catch (InvalidUdpReplyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sendRequest(byte[] bytes, DatagramSocket udpSocket) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        udpSocket.send(packet);
    }

    private static DatagramSocket connectToTracker(String announceAddr) throws SocketException, UnknownHostException {
        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress addr = InetAddress.getByName(getAddr(announceAddr));
        int port = getPort(announceAddr);
        udpSocket.connect(addr, port);
        udpSocket.setSoTimeout(UDP_TIMEOUT);
        return udpSocket;
    }

    private static ByteBuffer getReplyBuffer(DatagramSocket udpSocket) throws IOException {
        byte[] datagramBuffer = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
        udpSocket.receive(recvPacket);
        ByteBuffer buffer = ByteBuffer.allocate(recvPacket.getLength());
        buffer.put(datagramBuffer, 0, recvPacket.getLength());
        buffer.flip();
        return buffer;
    }

    private static boolean validReceiveBuffer(ByteBuffer buffer, int expectedLength, int expectedAction, int expectedTransactionId) {
        if(buffer.remaining() >= expectedLength){
            int receivedAction = buffer.getInt();
            int receivedTransactionId = buffer.getInt();
            return expectedAction == receivedAction && expectedTransactionId == receivedTransactionId;
        }
        return false;
    }

    private static Long parseConnectionReply(DatagramSocket udpSocket, int expectedTransactionId) throws IOException, InvalidUdpReplyException {
        ByteBuffer buffer = getReplyBuffer(udpSocket);
        if(validReceiveBuffer(buffer, CONNECTION_RESP_MIN_LEN, CONNECT_ACTION, expectedTransactionId)){
            return buffer.getLong();
        }
        throw new InvalidUdpReplyException();
    }

    private static TrackerQueryResult parseAnnounceReply(DatagramSocket udpSocket, int expectedTransactionId) throws IOException, InvalidUdpReplyException {
        ByteBuffer buffer = getReplyBuffer(udpSocket);
        if(validReceiveBuffer(buffer, ANNOUNCE_RESP_MIN_LEN, ANNOUNCE_ACTION, expectedTransactionId)){
            return new UdpTrackerQueryResult(buffer);
        }
        throw new InvalidUdpReplyException();
    }

    private static byte[] generateConnectionRequest(int transactionId){
        ByteBuffer reqBuffer = ByteBuffer.allocate(CONNECTION_REQ_LEN);
        reqBuffer.putLong(CONNECTION_ID);
        reqBuffer.putInt(CONNECT_ACTION);
        reqBuffer.putInt(transactionId);
        return reqBuffer.array();
    }

    private static byte[] generateAnnounceRequest(TorrentFile torrentFile, Long connId, int transactionId, Tracker.Event event) {
        ByteBuffer buffer = ByteBuffer.allocate(ANNOUNCE_REQ_LEN);
        buffer.putLong(connId);
        buffer.putInt(ANNOUNCE_ACTION);
        buffer.putInt(transactionId);
        buffer.put(torrentFile.getTorrentId().getBytes());
        buffer.put(torrentFile.getPeerId().getBytes());
        buffer.putLong(torrentFile.getDownloaded());
        buffer.putLong(torrentFile.getLeft());
        buffer.putLong(torrentFile.getUploaded());
        buffer.putInt(event.id);
        buffer.putInt(0); //Ip address, default 0
        buffer.putInt(0); //TODO decide what to put in key field
        buffer.putInt(-1); // Numwant, get default by sending -1
        buffer.put(getLocalPortBytes());
        return buffer.array();
    }

    private static int generateTransactionId() {
        return new Random().nextInt();
    }

    private static String getAddr(String announce){
        return announce.substring(6, announce.lastIndexOf(":"));
    }

    private static int getPort(String announce){
        int portBegin = announce.lastIndexOf(":") + 1;
        int postPortSlash = announce.indexOf("/", portBegin);
        int portEnd = postPortSlash == -1 ? announce.length() : postPortSlash;
        return Integer.parseInt(announce.substring(portBegin, portEnd));
    }

    private static byte[] getLocalPortBytes() {
        int port = Client.PORT;
        byte[] portBytes = new byte[2];
        portBytes[0] = (byte) (port & 0xFF);
        portBytes[1] = (byte)((port >> 8) & 0xFF);
        return portBytes;
    }

    private static class InvalidUdpReplyException extends Exception{}
}
