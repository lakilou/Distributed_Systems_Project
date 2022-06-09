import java.io.IOException;
import java.util.*;

interface Broker extends Node {

	ArrayList<BrokerImpl> brokers = new ArrayList<BrokerImpl>();


	void receiveMessages() throws InterruptedException;
	void notifyPublisher(Connection connection, BrokerMessage message) throws IOException;
	void pull(Connection connection, BrokerMessage message, Queue<byte[]> queue) throws IOException, ClassNotFoundException;
	void filterConsumers(ConsumerMessage message, Set<PublisherInfo> listOfPublisherForThatHashtagWithoutMe);
}