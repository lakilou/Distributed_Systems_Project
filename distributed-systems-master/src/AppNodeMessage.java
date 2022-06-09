import java.util.ArrayList;

public class AppNodeMessage extends Message{
    private ArrayList<BrokerImpl> allBrokers = null;

    public AppNodeMessage() {
        this.allBrokers = new ArrayList<BrokerImpl>();
    }

    public void setAllBrokers(ArrayList<BrokerImpl> allBrokers) {
        this.allBrokers = allBrokers;
    }

    public ArrayList<BrokerImpl> getAllBrokers() {
        return allBrokers;
    }
}
