package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;
import main.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;


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

    public TorrentFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        this.dict = dict;
        this.filesSaveFolder = saveFolder;

        this.info = new TreeMap<String, Object>((Map<String, Object>) this.dict.get("info"));
        this.name =(String) this.info.get("name");
        this.pieceSize = (Long) this.info.get("piece length");
        this.hash_pieces = (String) this.info.get("pieces");

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
        this.prepareInfoField();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
        benWriter.writeDict(this.info);
        byte[] infoString = out.toByteArray();
        this.infoHash = Utils.calculateHash(infoString);
    }

    protected abstract void prepareInfoField();

    public Map<String, Object> generateTorrent() throws NoSuchAlgorithmException {


        this.torrent = new HashMap<String,Object>();
        this.torrent.put ("name",this.name);
        this.torrent.put("piece length",this.len_piece);
        this.hash_pieces = (Utils.calculateHash((this.pieces).getBytes())).toString();
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

    public String getName() { return this.name; }

    public String getTrackerAnnounce() {
        return this.announce;
    }


    /**
     * Obtains the Read/Write structure helper, with one implementation for each of the torrent types
     * @param index the first piece index
     * @param begin the position inside the first piece
     * @param length the length of the data to be read (may contain multiple pieces and/or files)
     * @return the structure which contains a list of objects which point to the files to be read, one for each file and the methods needed to read it
     */
    public abstract TorrentBlock getFileBlock(int index, int begin, int length);

    public abstract Long getLength();


    public boolean isPieceValid(byte[] piece, int pieceIndex) throws NoSuchAlgorithmException {
        byte[] expected = this.hash_pieces.substring(pieceIndex * 20, pieceIndex * 20 + 20).getBytes(StandardCharsets.ISO_8859_1);
        byte[] pieceHash = Utils.calculateHash(piece);
        return Arrays.equals(expected, pieceHash);
    }

    public Long getPieceSize() {
        return pieceSize;
    }

    protected int calculateStartingPosition(int index, int begin){
        return (int) (index * this.pieceSize + begin);
    }

    public int getValidReadLength(int index, int begin, int length) {
        int expectedReadEnd = this.calculateStartingPosition(index, begin) + length;
        if(expectedReadEnd > this.getLength()){
            return (int) (this.getLength() - this.calculateStartingPosition(index, begin)); //limit the read to length of file
        } else {
            return length;
        }
    }

    public abstract void verifyAndAllocateFiles();
}
