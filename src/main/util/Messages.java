package main.util;

/**
 * Created by marcelo on 12/11/16.
 */
public enum Messages {
    DISPATCHER_RUN("Dispatcher running"), CONNECTION_ACCEPT("Accepting new connection"), WRITE_CONNECTION("Writing to output buffer"),
    REQUEST_PROCESS_FAIL("Processing of piece request failed"), PEER_CONNECTION_ACCEPT("Peer created after accepting connection"),
    PEER_CONNECTION_AUTO_CREATE("Trying to connect to other peer"), SEND_HANDSHAKE("Sending Handshake"), SEND_BITFIELD("Sending Bitfield"),
    SEND_STATE_CHANGE("Sending State Change Message"), SEND_HAVE("Sending Have Messages"),
    PEER_SHUTDOWN("Closing peer connection"), SOCKET_CLOSE_FAIL("Failed to close socket, ignoring..."),
    SOCKET_READ_FAIL("Failed to read from peer socket, closing connection"), SOCKET_WRITE_FAIL("Failed to write from peer socket, closing connection"),
    FAILED_CONNECT_PEER("Failed to connect to Peer"), TRACKER_UNREACHABLE("Can't Connect to Tracker"), TRACKER_CONNECT_SUCCCESS("Connected to tracker, rescheduling for default period");

    private final String text;

    public String getText(){ return this.text; }

    Messages(String message) {
        this.text = message;
    }
}
