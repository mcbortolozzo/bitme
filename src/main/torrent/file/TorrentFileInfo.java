package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;
import main.torrent.HashId;
import main.torrent.TorrentFile;
import sun.security.provider.SHA;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class TorrentFileInfo {

    private Long pieceSize;
    private byte[] infoHash;

    //The .torrent to create
    private Map<String,Object> torrent;

    private Map<String,Object> dict;
    protected Map<String,Object> info;
    protected List<Map<String,Object>> files;
    private String announce;
    private List<String> l_announce ;
    private Long date;
    private String comment;
    private String created_by;
    private Long len_piece;
    private String pieces;
    private String name;
    protected List<Long> len_file;

    public TorrentFileInfo(Map<String, Object> dict) throws IOException, NoSuchAlgorithmException {
        this.dict = dict;

        this.info = (Map<String,Object>) this.dict.get("info");
        this.name =(String) this.info.get("name");
        this.pieceSize = (Long) this.info.get("piece length");
        this.pieces = (String) this.info.get("pieces");
        this.len_file = new ArrayList<Long>();

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

        generateInfoHash();
    }

    private void generateInfoHash() throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out);
        benWriter.writeDict(this.info);
        String infoString = new String(out.toByteArray());
        this.infoHash = calculateHash(infoString);
    }

    protected byte[] calculateHash(String infoString) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA1");
        crypt.reset();
        crypt.update(infoString.getBytes());
        return crypt.digest();
    }

    public void generateTorrent() {

        this.torrent = new HashMap<String,Object>();

        this.torrent.put ("name",this.name);
        this.torrent.put("piece length",this.len_piece);
        this.torrent.put("pieces",this.pieces);
        this.torrent.put("announce",this.announce);
        this.torrent.put("announce-list",this.l_announce);
        this.torrent.put("comment",this.comment);
        this.torrent.put("created by",this.created_by);
        this.date = new Date().getTime() ;
        this.torrent.put("creation date",this.date);


    }

    public FileOutputStream bencodedFile(String nameFile) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out);
        benWriter.writeDict(this.torrent);
        FileOutputStream file = new FileOutputStream(new File(nameFile));
        out.writeTo(file);
        return file;
    }

    public byte[] getInfoHash() {
        return this.infoHash;
    }

    public int getPieceCount(){
        return this.pieces.length() / 20;
    }
}
