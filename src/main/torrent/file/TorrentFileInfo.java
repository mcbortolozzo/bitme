package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;
import main.util.Utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;


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
    protected Map<String,Object> torrent;
    protected Map<String,Object> information;
    private String hash_pieces;

    private Map<String,Object> dict;
    protected TreeMap<String,Object> info;
    protected List<Map<String,Object>> files;
    private String announce;
    private List<List<String>> l_announce ;
    private Long date;
    private String comment;
    private String created_by;
    private Long len_piece;
    private String pieces;
    protected String name;



    Logger logger = Logger.getLogger(TorrentFileInfo.class.getName());
    public TorrentFileInfo(String filesSaveFolder, String name){
        this.filesSaveFolder = filesSaveFolder;
        this.name = name;
        this.torrent = new HashMap<String,Object>();
        this.information = new HashMap<String, Object>();
    }

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
            this.l_announce = (List<List<String>>) this.dict.get("announce-list");
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

    /**
     * generates the dictionnary that contains the torrent to be informations
     * @param file : list of sources files
     * @param directoryName : source
     * @param announce
     * @param comment
     * @param piece_Length
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public TorrentFileInfo generateTorrent(File file, String directoryName, String[] announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {
        this.pieceSize = (long) (piece_Length * 1024);
        logger.info("generate dictionnary");
        this.information.put ("name", directoryName);
        this.information.put("piece length",piece_Length);
        this.hash_pieces();
        this.information.put("pieces",this.hash_pieces);
        this.torrent.put("info",this.information);
        this.torrent.put("announce",announce[0]);
        if(announce.length > 1){
            String[] l_announce = Arrays.copyOfRange(announce, 1, announce.length);
            this.torrent.put("announce-list",l_announce);
        }
        this.torrent.put("comment",comment);
        this.torrent.put("created by","bitMe alpha v0.33");
        this.date = (new Date().getTime())/1000;
        this.torrent.put("creation date",this.date);
        logger.info("dicttionnary: "+this.torrent.toString());
        return this;
    }

    /**
     * hash piece per piece
     * @param files list of source files
     * @param piece_length
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void hash_pieces () throws IOException, NoSuchAlgorithmException {
        logger.info("hash pieces ");
        this.hash_pieces = "";
        int piece = 0;
        while(calculateStartingPosition(piece, 0) < this.getLength()){
            TorrentBlock tb = this.getFileBlock(piece, 0, Math.toIntExact(this.getPieceSize()));
            ByteBuffer buffer = tb.readFileBlock();
            hash_pieces += new String(Utils.calculateHash(buffer.array()), StandardCharsets.ISO_8859_1);
            piece++;
        }
    }

    /**
     * Creates a bencoded file based on a dictionnary
     * @param nameFile : name of the torrent file to create
     * @return
     * @throws IOException
     */
    public FileOutputStream bencodedFile(String nameFile) throws IOException {
        logger.info("bencode file");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeWriter benWriter = new BencodeWriter(out, StandardCharsets.ISO_8859_1);
        benWriter.writeDict(this.torrent);
        logger.info("namefile: "+nameFile);
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

    public String getTrackerAnnounce() throws InvalidObjectException {
        if(this.announce.substring(0,4).equals("http")){
            return this.announce;
        } else {
            for(List<String> tracker : this.l_announce){
                if(tracker.get(0).substring(0,4).equals("http")){
                    return tracker.get(0);
                }
            }
        }
        throw new InvalidObjectException("No http trackers in torrent");
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

    protected Long calculateStartingPosition(int index, int begin){
        return (index * this.pieceSize + begin);
    }

    public int getValidReadLength(int index, int begin, int length) {
        Long expectedReadEnd = this.calculateStartingPosition(index, begin) + length;
        if(expectedReadEnd > this.getLength()){
            return (int) (this.getLength() - this.calculateStartingPosition(index, begin)); //limit the read to length of file
        } else {
            return length;
        }
    }

    public abstract void verifyAndAllocateFiles();
}
