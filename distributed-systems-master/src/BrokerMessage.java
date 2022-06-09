import java.util.ArrayList;

public class BrokerMessage extends Message {
    private String search;
    private String videoName;
    VideoInfoTransfer videoTransfer;

    public BrokerMessage(String search) {
        this.search = search;
        videoTransfer = new VideoInfoTransfer();
    }

    public String getSearch() {
        return search;
    }
}
