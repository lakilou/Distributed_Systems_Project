import java.io.IOException;
import java.util.*;

interface AppNode extends Node {

//	// Enimeronetai kathe fora pou kapoios kanei register se emena.
//	ArrayList<AppNodeImpl> followers = new ArrayList<AppNodeImpl>(); // KAINOURGIO
//	// Enimeronetai kathe fora pou kano register se kapoion.
//	ArrayList<AppNodeImpl> following = new ArrayList<AppNodeImpl>(); // KAINOURGIO

	// Not used, but maybe someday ll use it
	public BrokerImpl getRandomElement(ArrayList<BrokerImpl> list);

	// Publisher
	void addHashTag(String hashtag) throws IOException;
	void removeHashTag(String hashtag);
	void preparePushForHashtag(BrokerMessage message);
	void preparePushForChannelName(BrokerMessage message, ArrayList<VideoFile> listOfVideoFile);
	void push(Connection connection, BrokerMessage message, ArrayList<VideoFile> listOfVideoFile) throws IOException, ClassNotFoundException;
	public Set<BrokerImpl> getBrokerList(); // ok
	void notifyBrokerForHashtagsOrChannelName(Connection con, String hashTag, BrokerImpl b) throws IOException, InterruptedException;


	// Consumer
	void register(String s);
	void playData(String s,  ArrayList<byte []> v) throws IOException;
}