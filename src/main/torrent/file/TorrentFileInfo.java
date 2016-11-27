package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
public abstract class TorrentFileInfo {

    protected Long pieceSize;
    private byte[] infoHash;
    protected String filesSaveFolder;

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
    protected String name;
    protected List<Long> len_file;

    public TorrentFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        this.dict = dict;
        this.filesSaveFolder = saveFolder;

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
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
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

    public byte[] getInfoHash() {
        return this.infoHash;
    }

    public int getPieceCount(){
        return this.pieces.length() / 20;
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

}
