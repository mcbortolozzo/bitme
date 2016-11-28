package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;


import java.io.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public abstract class TorrentFileInfo {

    protected Long pieceSize;
    private byte[] infoHash;
    protected String filesSaveFolder;

    //The .torrent to create
    private Map<String,Object> torrent;

    private Map<String,Object> dict;
    protected TreeMap<String,Object> info;
    protected List<Map<String,Object>> files;
    private String announce;
    private List<String> l_announce ;
    private Long date;
    private String comment;
    private String created_by;
    private Long len_piece;
    private String hash_pieces;
    private String pieces;
    protected String name;
    protected List<Long> len_file;

    public TorrentFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        this.dict = dict;
        this.filesSaveFolder = saveFolder;

        this.info = new TreeMap<String, Object>((Map<String, Object>) this.dict.get("info"));
        this.name =(String) this.info.get("name");
        this.pieceSize = (Long) this.info.get("piece length");
        this.hash_pieces = (String) this.info.get("pieces");
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
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
        benWriter.writeDict(this.info);
        byte[] infoString = out.toByteArray();
        this.infoHash = calculateHash(infoString);
    }

    public static byte[] calculateHash(byte[] infoString) throws NoSuchAlgorithmException {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(infoString);
        return crypt.digest();
    }

    public Map<String, Object> generateTorrent() throws NoSuchAlgorithmException {


        this.torrent = new HashMap<String,Object>();
        this.torrent.put ("name",this.name);
        this.torrent.put("piece length",this.len_piece);
        this.hash_pieces = (calculateHash((this.pieces).getBytes())).toString();
        this.torrent.put("pieces",this.hash_pieces);
        this.torrent.put("announce",this.announce);
        this.torrent.put("announce-list",this.l_announce);
        this.torrent.put("comment",this.comment);
        this.torrent.put("created by",this.created_by);
        this.date = new Date().getTime();
        this.torrent.put("creation date",this.date);
        return this.torrent;


    }

    public FileOutputStream bencodedFile(String nameFile) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
        benWriter.writeDict(this.torrent);
        FileOutputStream file = new FileOutputStream(new File(nameFile));
        out.writeTo(file);
        return file;
    }

    public byte[] getInfoHash() {
        return this.infoHash;
    }

    public int getPieceCount(){
        return this.hash_pieces.length() / 20;
    }

    public String getTrackerAnnounce() {
        return this.announce;
    }


    public abstract TorrentBlock getFileBlock(int index, int begin, int length);

    public abstract Long getLength();


}
