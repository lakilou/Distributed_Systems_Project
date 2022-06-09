public class ConsumerInfo extends AppNodeInfo {

    public ConsumerInfo(String channelName, String ip, int port) {
        this.channelName = channelName;
        this.ip = ip;
        this.port = port;
    }
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
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