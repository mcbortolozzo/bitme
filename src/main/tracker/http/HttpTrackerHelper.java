package main.tracker.http;

import main.Client;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.tracker.Tracker;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by marcelo on 19/11/16.
 */
public class HttpTrackerHelper {

    private static class HttpField{
        String fieldName;
        String fieldValue;

        public HttpField(Tracker.Field field, String fieldValue) {
            this.fieldName = field.name().toLowerCase();
            this.fieldValue = fieldValue;
        }

        public HttpField(Tracker.Field field, int fieldValue) {
            this.fieldName = field.name().toLowerCase();
            this.fieldValue = String.valueOf(fieldValue);
        }

        public HttpField(Tracker.Field field, Long fieldValue) {
            this.fieldName = field.name().toLowerCase();
            this.fieldValue = String.valueOf(fieldValue);
        }

        @Override
        public String toString() {
            return this.fieldName + "=" + this.fieldValue;
        }
    }

    public static String generateTrackerRequest(HashId torrentId, Tracker.Event event, String trackerURL) throws MalformedURLException, UnsupportedEncodingException {
        List<HttpField> parameters = getRequestParameters(torrentId, event);
        return composeTrackerRequest(parameters, trackerURL);
    }

    //TODO compact response, accept or not?
    //TODO ip, numwant, key and tracker id are optional, should we add?
    public static byte[] sendTrackerRequest(String requestURL) throws IOException {
        URL request = new URL(requestURL);
        InputStream in = new BufferedInputStream(request.openStream());
        return getRequestReply(in);
    }

    private static byte[] getRequestReply(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while(-1 != (n = in.read(buf))){
            out.write(buf, 0, n);
        }
        in.close();
        out.close();
        return out.toByteArray();
    }

    private static List<HttpField> getRequestParameters(HashId torrentId, Tracker.Event event) throws UnsupportedEncodingException {
        int port = Client.PORT;
        List<HttpField> parameters = new LinkedList<>();
        parameters.add(new HttpField(Tracker.Field.PORT, port));
        parameters.addAll(getTorrentFileParameters(torrentId));
        if(!event.equals(Tracker.Event.UNSPECIFIED)){
            parameters.add(new HttpField(Tracker.Field.EVENT, event.name().toLowerCase()));
        }
        return parameters;
    }

    private static List<HttpField> getTorrentFileParameters(HashId torrentId) throws UnsupportedEncodingException {
        TorrentFile torrentFile = TorrentManager.getInstance().retrieveTorrent(torrentId);
        List<HttpField> fileParameters = new LinkedList<>();
        String encodedTorrentHash = torrentFile.getTorrentId().asURLEncodedString();
        String encodedPeerHash = torrentFile.getPeerId().asURLEncodedString();
        fileParameters.add(new HttpField(Tracker.Field.INFO_HASH, encodedTorrentHash));
        fileParameters.add(new HttpField(Tracker.Field.PEER_ID, encodedPeerHash));
        fileParameters.add(new HttpField(Tracker.Field.UPLOADED, torrentFile.getUploaded()));
        fileParameters.add(new HttpField(Tracker.Field.DOWNLOADED, torrentFile.getDownloaded()));
        fileParameters.add(new HttpField(Tracker.Field.LEFT, torrentFile.getLeft()));
        fileParameters.add(new HttpField(Tracker.Field.NUMWANT, 50));
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

}
