package main.torrent.file;

import main.util.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
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
            for(String p : this.path){ pathString += p + "/"; }
            return pathString;
        }

        public String getFullPath() {
            StringBuilder sb = new StringBuilder();
            sb.append(filesSaveFolder).append('/').append(name).append('/').append(this.getPath());
            return sb.toString();
        }

    }

    public MultipleFileInfo(String filesSaveFolder, String name){
        super(filesSaveFolder, name);
        this.files = new LinkedList<>();
    }

    public MultipleFileInfo(Map<String, Object> dict, String saveFolder) throws IOException, NoSuchAlgorithmException {
        super(dict, saveFolder);
        this.files = new LinkedList<>();
        List<Map<String, Object>> fileDataList = (List<Map<String,Object>>) this.info.get("files");

        extractFiles(fileDataList);
    }

    private void extractFiles(List<Map<String, Object>> fileDataList) {
        for (Map<String,Object> f : fileDataList){
            SubFileStructure subFile = new SubFileStructure((ArrayList<String>) f.get("path"), (Long) f.get("length"));
            this.files.add(subFile);
            if (f.containsKey("md5sum")){
                subFile.md5sum = (String) f.get("md5sum");
            }
        }
    }

    @Override
    public TorrentFileInfo generateTorrent(File file, String directoryName, String[] announce, String comment, int piece_Length) throws NoSuchAlgorithmException, IOException {
        this.pieceSize = (long) piece_Length;
        List<Map<String, Object>> filesTorrent = getFiles(file, file.getPath());
        this.information.put("files", filesTorrent);
        this.extractFiles(filesTorrent);
        super.generateTorrent(file, directoryName,  announce,  comment, piece_Length);
        return this;
    }

    private List<Map<String, Object>> getFiles(File file, String parentPath){
        List<Map<String, Object>> result = new LinkedList<>();
        List<File> subFiles = Arrays.asList(file.listFiles());
        Collections.sort(subFiles);
        for(File f : subFiles){
            if(f.isFile()){
                Map<String, Object> fileMap = new TreeMap<>();
                fileMap.put("length", f.length());
                String fileSeparator = System.getProperty("file.separator");
                String pattern = Pattern.quote(fileSeparator);
                String[] path = f.getPath().replace(parentPath + fileSeparator, "").split(pattern);
                List<String> L_path = new ArrayList<>(Arrays.asList(path));
                fileMap.put("path", L_path);
                result.add(fileMap);
            } else if (f.isDirectory()){
                result.addAll(getFiles(f, parentPath));
            }
        }
        return result;
    }

    @Override
    public TorrentBlock getFileBlock(int index, int begin, int length) {
        int blockSize = this.getValidReadLength(index, begin, length);
        TorrentBlock torrentBlock = new TorrentBlock(index, begin, blockSize);
        Long startingPosition = (index * this.pieceSize + begin);
        Long position = startingPosition;
        while(position - startingPosition < blockSize){ //read < length
            int lengthLeft = (int) (blockSize - (position - startingPosition));
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
    private FileBlockInfo getNextBlock(Long blockPositionBegin, int lengthLeft){
        Long fileBegin = 0l;
        Long currentPosition = 0l;
        for(SubFileStructure f : this.files){
            currentPosition += f.length;
            if(blockPositionBegin < currentPosition){
                Long positionInBlock = blockPositionBegin - fileBegin;
                int blockReadLength;
                if (lengthLeft <= (f.length - positionInBlock)) blockReadLength = lengthLeft;
                else blockReadLength = (int) (f.length - positionInBlock);
                return new FileBlockInfo(f.getFullPath(), positionInBlock, blockReadLength);
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
