package main.torrent.file;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class SingleFileInfo extends TorrentFileInfo {
    private Long len_file;
    private String md5sum;

    public SingleFileInfo(Map<String, Object> dict) throws IOException, NoSuchAlgorithmException {
        super(dict);

        this.len_file = (Long) this.info.get("length");
        if (this.info.containsKey("md5sum")){
            this.md5sum = (String) this.info.get("md5sum");
        }
    }

    @Override
    public Map<String, Object> generateTorrent() throws NoSuchAlgorithmException {
        Map<String,Object> torrent =  super.generateTorrent();
        torrent.put("length",this.len_file);
        torrent.put("md5sum",this.md5sum);
        return torrent ;
    }
}
