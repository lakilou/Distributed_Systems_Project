import java.util.ArrayList;

public class ConsumerMessage extends Message{
    private String search;
    VideoInfoTransfer videoTransfer;

    private int howManyVideos;
    int howManyPublisherHaveTheHashtag;
    int sizeOfQueue;
    boolean findIt;
    String notFound;
    String register;

    public ConsumerMessage(String myChannelName, String search, String ip, int port) {
        this.search = search;
        this.channelName = myChannelName;
        videoTransfer = new VideoInfoTransfer();
        this.ip = ip;
        this.port = port;
    }

    public String getChannelName() {
        return channelName;
    }
    public int getHowManyVideos() {
        return howManyVideos;
    }
    public String getSearch() {
        return search;
    }
    public String getIp() {
        return ip;
    }
    public int getPort() {
        return port;
    }
}
