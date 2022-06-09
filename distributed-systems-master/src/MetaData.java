import java.io.Serializable;
import java.util.ArrayList;

public class MetaData implements Serializable {
    String dateCreated = null;
    String length = null;
    String framerate = null;
    String frameWidth = null;
    String frameHeight = null;
    String fileType;

    public MetaData(String dateCreated, String length, String framerate, String frameWidth, String frameHeight,String fileType) {
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.fileType = fileType;
    }
}
