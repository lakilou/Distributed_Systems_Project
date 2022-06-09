import java.util.ArrayList;

public class PublisherMessage extends Message{

    private String hashTag;
    private String videoName;
    ArrayList<String> videoNames;

    public PublisherMessage(String hashTag, String channelName, String ip, int port) {

        this.hashTag = hashTag;
        this.channelName = channelName;
        this.ip = ip;
        this.port = port;
    }
    public String getVideoName() {
        return videoName;
    }
    public String getHashTag() {
        return hashTag;
    }
    public String getChannelName() {
        return channelName;
    }
    public String getIp() {
        return ip;
    }
    public int getPort() {
        return port;
    }
}
