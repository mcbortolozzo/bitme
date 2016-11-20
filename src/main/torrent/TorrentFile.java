package src.main.torrent;
import com.hypirion.bencode.BencodeReadException;
import com.hypirion.bencode.BencodeReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by marcelo on 07/11/16.
 */
public class TorrentFile {


    private Map<String,Object> dict;
    private Map<String,Object> info;
    private List<Map<String,Object>> files;
    private String announce;
    private List<String> l_announce ;
    private Long date;
    private String comment;
    private String created_by;
    private Long len_piece;
    private String pieces;
    private String name;
    private List<Long> len_file;
    private List<String> md5sum;
    private List<String> path;

    public TorrentFile(){
        this.l_announce =null;
        this.date =null;
        this.comment =null;
        this.created_by=null;
        this.files=null;

    }

    public void parseTorrent(String namefile)  {

        InputStream in = null;
        try {
            in = new FileInputStream(namefile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BencodeReader benReader = new BencodeReader(in);
        try {
            this.dict = benReader.readDict();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BencodeReadException e) {
            e.printStackTrace();
        }
        this.info = (Map<String,Object>) this.dict.get("info");
        this.name =(String) this.info.get("name");
        this.len_piece = (Long) this.info.get("piece length");
        this.pieces = (String) this.info.get("pieces");
        this.len_file = new ArrayList<Long>();
        this.path = new ArrayList<String>();
        this.md5sum = new ArrayList<String>();
        if (this.info.containsKey("files")){
            this.files = (List<Map<String,Object>>) this.info.get("files");

            for (Map<String,Object> f : this.files){
                this.len_file.add((Long) f.get("length"));
                this.path.add((String) f.get("path"));
                if (f.containsKey("md5sum")){
                    this.md5sum.add((String) f.get("md5sum"));
                }
            }
        }
        else{
            this.len_file.add((Long) this.info.get("length"));
            if (this.info.containsKey("md5sum")){
                this.md5sum.add((String) this.info.get("md5sum"));
            }

        }
        this.announce = (String) this.dict.get("announce");
        if ( this.dict.containsKey("creation date")) {
            this.date = (Long) this.dict.get("creation date");
        }
        if (this.dict.containsKey("comment")) {
            this.comment = (String) this.dict.get("comment");
        }

        if (this.dict.containsKey("created by")) {
            this.created_by = (String) this.dict.get("created by");
        }
        if (this.dict.containsKey(("announce-list"))){
            this.l_announce = (List<String>) this.dict.get("announce-list");
        }





    }


/**
    private String torrentId;
    //TODO complete constructor and class methods
    public TorrentFile(String torrentId){
        this.torrentId = torrentId;
    }

    public String getTorrentId() {
        return torrentId;
    }
**/
}
