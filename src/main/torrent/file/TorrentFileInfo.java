package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;
import main.util.Utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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
        this.prepareInfoField();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
        benWriter.writeDict(this.info);
        byte[] infoString = out.toByteArray();
        this.infoHash = Utils.calculateHash(infoString);
    }

    protected abstract void prepareInfoField();

    public Map<String, Object> generateTorrent(File file,String announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {


        this.torrent = new HashMap<String,Object>();
        this.info = new TreeMap<String, Object>();

        this.info.put ("name",file.getName());
        this.info.put("piece length",piece_Length);
        this.hash_pieces(file,piece_Length);
        this.info.put("pieces",this.hash_pieces);
        this.torrent.put("info",this.info);
        this.torrent.put("announce",announce);
        this.torrent.put("announce-list",this.l_announce);
        this.torrent.put("comment",comment);
        this.torrent.put("created by","bitMe alpha v0.33");
        this.date = (new Date().getTime())/1000;
        this.torrent.put("creation date",this.date);
        return this.torrent;

    }


    public void hash_pieces ( File file, int piece_length ) throws IOException, NoSuchAlgorithmException {
        FileInputStream f = new FileInputStream(file);
        ByteBuffer buffer = ByteBuffer.allocate(piece_length);
        FileChannel channel = f.getChannel();
        this.hash_pieces = "";
        while ( channel.read(buffer) > 0){
            byte [] piece= Utils.calculateHash(buffer.array());
            this.hash_pieces += piece.toString();
        }
        if (buffer.position() > 0){
            buffer.limit(buffer.position());
            buffer.position(0);
            byte [] piece= Utils.calculateHash(buffer.array());
            this.hash_pieces += piece.toString();
        }


    }

    public FileOutputStream generateFile(String torrentPath, File file,String announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {
        this.generateTorrent(file, announce, comment, piece_Length);
        return this.bencodedFile(torrentPath);
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
}
