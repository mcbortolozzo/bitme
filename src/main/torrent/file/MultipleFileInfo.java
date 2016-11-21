package main.torrent.file;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class MultipleFileInfo extends TorrentFileInfo {
    private List<Long> len_file;
    private List<String> path;
    private List<String> md5sum;


    public MultipleFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        super(dict, saveFolder);
        this.files = (List<Map<String,Object>>) this.info.get("files");
        this.path = new ArrayList<String>();
        this.md5sum = new ArrayList<String>();

        for (Map<String,Object> f : this.files){
            this.len_file.add((Long) f.get("length"));
            this.path.add((String) f.get("path"));
            if (f.containsKey("md5sum")){
                this.md5sum.add((String) f.get("md5sum"));
            }
        }
    }

    @Override
    public TorrentBlock getFileBlock(int index, int begin, int length) {
        return null;
    }

    @Override
    public Long getLength() {
        Long totalLength = 0L;
        for(Long l : len_file){
            totalLength += l;
        }
        return totalLength;
    }
}
