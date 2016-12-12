package main.tracker;

import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by marcelo on 19/11/16.
 */
public class TrackerHelper {

    private static final String ANNOUNCE_PREFIX = "/announce";

    public enum Event{
        STARTED, STOPPED, COMPLETED, UNSPECIFIED;
    }

    private enum Field {
        INFO_HASH, PEER_ID, PORT, UPLOADED, DOWNLOADED, LEFT, COMPACT, NO_PEER_ID,
        EVENT, IP, NUMWANT, KEY, TRACKERID;
    }

    private static class HttpField{
        String fieldName;
        String fieldValue;

        public HttpField(Field field, String fieldValue) {
            this.fieldName = field.name().toLowerCase();
            this.fieldValue = fieldValue;
        }

        public HttpField(Field field, int fieldValue) {
            this.fieldName = field.name().toLowerCase();
            this.fieldValue = String.valueOf(fieldValue);
        }

        @Override
        public String toString() {
            return this.fieldName + "=" + this.fieldValue;
        }
    }

    public static String generateTrackerRequest(HashId torrentId, Event event, String trackerURL) throws MalformedURLException, UnsupportedEncodingException {
        List<HttpField> parameters = getRequestParameters(torrentId, event);
        return composeTrackerRequest(parameters, trackerURL);
    }

    //TODO compact response, accept or not?
    //TODO ip, numwant, key and tracker id are optional, should we add?
    public static String sendTrackerRequest(String requestURL) throws IOException {
        URL request = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) request.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return getRequestReply(reader);
    }

    private static String getRequestReply(BufferedReader reader) throws IOException {
        StringBuilder reply = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            reply.append(line);
        }
        reader.close();
        return reply.toString();
    }

    private static List<HttpField> getRequestParameters(HashId torrentId, Event event) throws UnsupportedEncodingException {
        int port = Client.PORT;
        List<HttpField> parameters = new LinkedList<>();
        parameters.add(new HttpField(Field.PORT, port));
        parameters.addAll(getTorrentFileParameters(torrentId));
        if(!event.equals(Event.UNSPECIFIED)){
            parameters.add(new HttpField(Field.EVENT, event.name().toLowerCase()));
        }
        return parameters;
    }

    //TODO escape the hash parameters
    private static List<HttpField> getTorrentFileParameters(HashId torrentId) throws UnsupportedEncodingException {
        TorrentFile torrentFile = TorrentManager.getInstance().retrieveTorrent(torrentId);
        List<HttpField> fileParameters = new LinkedList<>();
        String encodedTorrentHash = torrentFile.getTorrentId().asURLEncodedString();
        String encodedPeerHash = torrentFile.getPeerId().asURLEncodedString();
        fileParameters.add(new HttpField(Field.INFO_HASH, encodedTorrentHash));
        fileParameters.add(new HttpField(Field.PEER_ID, encodedPeerHash));
        fileParameters.add(new HttpField(Field.UPLOADED, torrentFile.getUploaded()));
        fileParameters.add(new HttpField(Field.DOWNLOADED, torrentFile.getDownloaded()));
        fileParameters.add(new HttpField(Field.LEFT, torrentFile.getLeft()));
        return fileParameters;
    }

    private static String composeTrackerRequest(List<HttpField> messageArgs, String trackerURL) throws MalformedURLException {
        StringBuilder request = new StringBuilder();
        request.append(trackerURL).append("?");
        for(HttpField arg : messageArgs){
            request.append(arg.toString());
            if(messageArgs.indexOf(arg) < messageArgs.size() - 1){
                request.append("&");
            }
        }
        return request.toString();
    }

    public static String byteToEscapedString(byte[] bytes){
        return "";
    }
}
