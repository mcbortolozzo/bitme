package main.torrent.file;

import com.hypirion.bencode.BencodeWriter;
import main.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class MultipleFileInfo extends TorrentFileInfo {
    private List<SubFileStructure> files;

    private class SubFileStructure {
        Long length;
        List<String> path;
        String md5sum;

        SubFileStructure(ArrayList<String> path, Long length) {
            this.length = length;
            this.path = path;
        }

        public String getPath(){
            String pathString = "";
            for(String p : this.path){ pathString += p; }
            return pathString;
        }

        public String getFullPath() {
            StringBuilder sb = new StringBuilder();
            sb.append(filesSaveFolder).append('/').append(name).append('/').append(this.getPath());
            return sb.toString();
        }

    }

    public MultipleFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        super(dict, saveFolder);
        this.files = new LinkedList<>();
        List<Map<String, Object>> fileDataList = (List<Map<String,Object>>) this.info.get("files");

        for (Map<String,Object> f : fileDataList){
            SubFileStructure subFile = new SubFileStructure((ArrayList<String>) f.get("path"), (Long) f.get("length"));
            this.files.add(subFile);
            if (f.containsKey("md5sum")){
                subFile.md5sum = (String) f.get("md5sum");
            }
        }
    }

    @Override
    public Map<String, Object> generateTorrent(List<File> file,String directoryName, String announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {
        List<Map<String, Object>> filesTorrent = new LinkedList<>();
        for (File f : file) {
            for (Map<String, Object> fi : filesTorrent) {
                fi.put("length", f.length());
                fi.put("path", f.getPath());
            }
        }
        this.information.put("files", filesTorrent);
        Map<String, Object> torrent = super.generateTorrent( file, directoryName,  announce,  comment, piece_Length);
        return torrent;

    }

    @Override
    public TorrentBlock getFileBlock(int index, int begin, int length) {
        int blockSize = this.getValidReadLength(index, begin, length);
        TorrentBlock torrentBlock = new TorrentBlock(index, begin, blockSize);
        int startingPosition = (int) (index * this.pieceSize + begin);
        int position = startingPosition;
        while(position - startingPosition < blockSize){ //read < length
            int lengthLeft = blockSize - position - startingPosition;
            FileBlockInfo nextBlock = getNextBlock(position, lengthLeft);
            if(nextBlock != null){
                position += nextBlock.getLength();
                torrentBlock.addNextBlock(nextBlock);
            }
        }
        return torrentBlock;
    }

    /**
     * Multiple file torrents may require reads that are split in different files, so this
     * method generates the block of data to be read from different files, based on the global position
     * in which it will be started and the amount left to be read.
     * @param blockPositionBegin the global torrent position of the read in bytes
     * @param lengthLeft the amount of bytes left to be read
     * @return an object containing the info required to read/write to a file, which is may be one of many files included in this read/write
     */
    private FileBlockInfo getNextBlock(int blockPositionBegin, int lengthLeft){
        int fileBegin = 0;
        int currentPosition = 0;
        for(SubFileStructure f : this.files){
            currentPosition += f.length;
            if(blockPositionBegin < currentPosition){
                int positionInBlock = blockPositionBegin - fileBegin;
                int blockReadLength;
                if (lengthLeft <= (f.length - positionInBlock)) blockReadLength = lengthLeft;
                else blockReadLength = Math.toIntExact(f.length);
                return new FileBlockInfo(f.getFullPath(), (long) positionInBlock, blockReadLength);
            }
            fileBegin = currentPosition;
        }
        return null;
    }

    @Override
    public Long getLength() {
        Long totalLength = 0L;
        for(SubFileStructure f : files){
            totalLength += f.length;
        }
        return totalLength;
    }

    @Override
    protected void prepareInfoField() {
        ArrayList<Map<String, Object>> oldFiles = (ArrayList<Map<String, Object>>) this.info.get("files");
        ArrayList<Map<String, Object>> filesList = oldFiles.stream().map(TreeMap::new).collect(Collectors.toCollection(ArrayList::new));
        this.info.put("files", filesList);
    }

    @Override
    public void verifyAndAllocateFiles() {
        for(SubFileStructure file : this.files){
            Utils.generateFile(file.getFullPath(), file.length);
        }
    }
}
