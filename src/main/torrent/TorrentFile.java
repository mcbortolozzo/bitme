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

    public Map<String, Object> getDict() {
        return dict;
    }

    public void setDict(Map<String, Object> dict) {
        this.dict = dict;
    }

    public List<Map<String, Object>> getFiles() {
        return files;
    }

    public void setFiles(List<Map<String, Object>> files) {
        this.files = files;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public List<String> getL_announce() {
        return l_announce;
    }

    public void setL_announce(List<String> l_announce) {
        this.l_announce = l_announce;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public Long getLen_piece() {
        return len_piece;
    }

    public void setLen_piece(Long len_piece) {
        this.len_piece = len_piece;
    }

    public String getPieces() {
        return pieces;
    }

    public void setPieces(String pieces) {
        this.pieces = pieces;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getLen_file() {
        return len_file;
    }

    public void setLen_file(List<Long> len_file) {
        this.len_file = len_file;
    }

    public List<String> getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(List<String> md5sum) {
        this.md5sum = md5sum;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
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
