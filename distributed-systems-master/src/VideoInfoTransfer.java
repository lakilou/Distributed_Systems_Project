import java.io.Serializable;
import java.util.ArrayList;

public class VideoInfoTransfer implements Serializable {
    private byte[] chunk;



    private ArrayList<Integer> chunkCounter;
    private ArrayList<String> videoNames;
    private ArrayList<String> associatedHashTags;

    public VideoInfoTransfer() {
        this.associatedHashTags = new ArrayList<String>();
        this.chunkCounter = new ArrayList<Integer>();
        this.videoNames = new ArrayList<String>();
    }

    public ArrayList<String> getVideoNames() {
        return videoNames;
    }
    public ArrayList<String> getAssociatedHashTags() {
        return associatedHashTags;
    }
    public void setChunkCounter(ArrayList<Integer> chunkCounter) {
        this.chunkCounter = chunkCounter;
    }
    public byte[] getChunk() {
        return chunk;
    }
    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }
    public void setVideoNames(ArrayList<String> videoNames) {
        this.videoNames = videoNames;
    }
    public ArrayList<Integer> getChunkCounter() {
        return chunkCounter;
    }
}
