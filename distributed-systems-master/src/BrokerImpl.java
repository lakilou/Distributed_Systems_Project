import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.math.BigInteger;
import java.util.function.Consumer;

public class BrokerImpl extends Thread implements Broker, Comparable<BrokerImpl>{
	public int compareTo(BrokerImpl st){
		return this.brokerHash.compareTo(st.brokerHash);
	}

	String brokerName;
	ServerSocket providerSocket;
	Socket connection = null;
	String ip;
	int port;
	BigInteger brokerHash;
	static int u = 1;

	Set<PublisherInfo> registeredPublisher = new HashSet<PublisherInfo>();
	Set<ConsumerInfo> registeredConsumer = new HashSet<ConsumerInfo>();

	HashMap<String, Set<PublisherInfo>> responsibleForHashtagsAndChannelNames = new HashMap<String, Set<PublisherInfo>>();
	HashMap<String, Set<ConsumerInfo>> whoRegisteredToWhom = new HashMap<String, Set<ConsumerInfo>>(); // KAINOURGIO

	public BrokerImpl(String ip, int port, String name) {
		this.brokerName = name;
		this.ip = ip;
		this.port = port;
		brokerHash = SHA1.hashText(ip+port);
	}
	@Override
	public void init(int port) throws IOException {
		providerSocket = new ServerSocket(port, 10);
	}
	@Override
	public void connect() {
		System.out.println("I am " + this.brokerName + " and waiting for a connection.");
		try {
			while (true) {
				connection = providerSocket.accept();
				this.receiveMessages();
			}
		} catch (IOException | InterruptedException ioException) {
			ioException.printStackTrace();
		} finally {
			this.disconnect();
		}
	}
	@Override
	public void receiveMessages() throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Connection connectWithSomeone = null;
				try {
					connectWithSomeone = new Connection(connection);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Message message;
				try {
					message = (Message) connectWithSomeone.in.readObject(); // received the hashtag or channel name 1
					if (message instanceof AppNodeMessage) {
						AppNodeMessage messageFromAppNode = (AppNodeMessage) message;

						messageFromAppNode.setAllBrokers(updateForAllBrokers());

						connectWithSomeone.out.writeObject(messageFromAppNode);
						connectWithSomeone.out.flush();
						connectWithSomeone.out.close();
						connectWithSomeone.in.close();
					}
					if (message instanceof PublisherMessage) {
						PublisherMessage messageFromPublisher = (PublisherMessage) message;

						PublisherInfo tempPublisher = returnPublisherFromRegisteredPublisher(registeredPublisher, messageFromPublisher.getChannelName(), messageFromPublisher.getIp(), messageFromPublisher.getPort());
						if(!containsPublisher(registeredPublisher, tempPublisher.channelName)) {
							registeredPublisher.add(tempPublisher);
						}
						if (!responsibleForHashtagsAndChannelNames.containsKey(messageFromPublisher.getHashTag())) {
							Set<PublisherInfo> publishers = new HashSet<>();
							publishers.add(tempPublisher);
							responsibleForHashtagsAndChannelNames.put(((PublisherMessage) message).getHashTag(), publishers);

							Set<ConsumerInfo> consumer = new HashSet<ConsumerInfo>();
							whoRegisteredToWhom.put(messageFromPublisher.getHashTag(), consumer);
						}
						else {
							responsibleForHashtagsAndChannelNames.get(messageFromPublisher.getHashTag()).add(tempPublisher);
						}


						Set<ConsumerInfo> subscribers = whoRegisteredToWhom.get(messageFromPublisher.getHashTag());

//						System.out.println("responsibleForHashtagsAndChannelNames" + responsibleForHashtagsAndChannelNames);
						connectWithSomeone.out.close();
						connectWithSomeone.in.close();
						//STELNOUME TO VIDEO ME TO HASHTAG POY MOLIS MAS ESTEILE O PUBLISHER
					}
					if (message instanceof ConsumerMessage) { // ConsumerMessage

						ConsumerMessage messageFromConsumer = (ConsumerMessage) message;
						Queue<byte[]> queue = new LinkedList<byte[]>();

						ArrayList<String> videoNames = new ArrayList<String>();
						ArrayList<Integer> chunkCounter = new ArrayList<Integer>();

						// Add the consumer on the consumer's list(registeredConsumer) and added to the hashTag's/channelName's list(whoRegisteredToWhom)
						if (messageFromConsumer.register.equals("yes")) {
							if (responsibleForHashtagsAndChannelNames.containsKey(messageFromConsumer.getSearch())) {
								ConsumerInfo tempConsumer = returnPublisherFromRegisteredConsumer(registeredConsumer, messageFromConsumer.getChannelName(), messageFromConsumer.getIp(), messageFromConsumer.getPort());
								if (!containsConsumer(registeredConsumer, tempConsumer.channelName)) {
									registeredConsumer.add(tempConsumer);
								}
								if (!tempConsumer.getChannelName().equals(messageFromConsumer.getSearch())) {
									whoRegisteredToWhom.get(messageFromConsumer.getSearch()).add(tempConsumer);
								}
//								if (!whoRegisteredToWhom.containsKey(messageFromConsumer.getSearch())) {
//									Set<ConsumerInfo> consumers = new HashSet<ConsumerInfo>();
//									consumers.add(tempConsumer);
//									whoRegisteredToWhom.put(messageFromConsumer.getSearch(), consumers);
//								} else {
//									whoRegisteredToWhom.get(messageFromConsumer.getSearch()).add(tempConsumer);
//								}
							}
						}
						System.out.println("registeredConsumer" + registeredConsumer);
						System.out.println("WhoRegisteredToWhom" + whoRegisteredToWhom);

						// Pull all videos with hashTag / channelName that equals search
						if (responsibleForHashtagsAndChannelNames.containsKey(messageFromConsumer.getSearch())) {
							Set<PublisherInfo> listOfPublisherForThatHashtag = responsibleForHashtagsAndChannelNames.get(messageFromConsumer.getSearch());
							filterConsumers(messageFromConsumer, listOfPublisherForThatHashtag);

							for (PublisherInfo p : listOfPublisherForThatHashtag) {
								if (!messageFromConsumer.getChannelName().equals(p.channelName)) {
									BrokerMessage messageForPublisher = new BrokerMessage(messageFromConsumer.getSearch());

									// Opening connection with each publisher
									Connection connectWithPublisher = new Connection(new Socket(p.ip, p.port));

									// Send to publisher the hashTag or channelName for search  1
									notifyPublisher(connectWithPublisher, messageForPublisher);

									// Received from publisher the chunkCounter and videoNames 2
									messageForPublisher = (BrokerMessage) connectWithPublisher.in.readObject();

									// Prepare the list of videoNames to filling the messageFromConsumer.setVideoNames
									for (String videoName : messageForPublisher.videoTransfer.getVideoNames()) {
										videoNames.add(videoName);
									}
									chunkCounter = messageForPublisher.videoTransfer.getChunkCounter();

									// take the videochunks and put it to the queue 3
									pull(connectWithPublisher, messageForPublisher, queue);

									connectWithPublisher.out.close();
									connectWithPublisher.in.close();
								}
							}
							// Sent to consumer the chunkCounter and videoNames 2
							messageFromConsumer.sizeOfQueue = queue.size();
							messageFromConsumer.videoTransfer.setVideoNames(videoNames);
							messageFromConsumer.videoTransfer.setChunkCounter(chunkCounter);
							messageFromConsumer.findIt = true;
							connectWithSomeone.out.writeObject(messageFromConsumer);
							// Send to consumer the video's chunks from queue
							while (!queue.isEmpty()) {
								messageFromConsumer = (ConsumerMessage) connectWithSomeone.in.readObject();
								messageFromConsumer.videoTransfer.setChunk(queue.remove());
								connectWithSomeone.out.writeObject(messageFromConsumer);
								connectWithSomeone.out.flush();
							}
						}
						else {
							messageFromConsumer.findIt = false;
							messageFromConsumer.notFound = "The hashtag or channelName you are searching on doesn't exist, sorry bro! Try again with something else.";
							connectWithSomeone.out.writeObject(messageFromConsumer);
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
				try {
					connectWithSomeone.out.close();
					connectWithSomeone.in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		//t.join();
	}
	public static boolean containsPublisher (Set<PublisherInfo> set, String channelName) {
		for (PublisherInfo o: set) {
			if (o.channelName.equals(channelName)) {
				return true;
			}
		}
		return false;
	}
	public static synchronized PublisherInfo returnPublisherFromRegisteredPublisher (Set<PublisherInfo> set, String channelName, String ip, int port) {
		PublisherInfo temp = new PublisherInfo(channelName, ip, port);
		for (PublisherInfo o: set) {
			if (o.channelName.equals(channelName)) {
				temp = o;
			}
		}
		return temp;
	}
	public static boolean containsConsumer (Set<ConsumerInfo> set, String channelName) {
		for (ConsumerInfo o: set) {
			if (o.channelName.equals(channelName)) {
				return true;
			}
		}
		return false;
	}
	public static ConsumerInfo returnPublisherFromRegisteredConsumer (Set<ConsumerInfo> set, String channelName, String ip, int port) {
		ConsumerInfo temp = new ConsumerInfo(channelName, ip, port);
		for (ConsumerInfo o: set) {
			if (o.channelName.equals(channelName)) {
				temp = o;
			}
		}
		return temp;
	}

	@Override
	public void notifyPublisher(Connection connection, BrokerMessage message) throws IOException {
		connection.out.writeObject(message);
		connection.out.flush();
	}
	@Override
	public void pull(Connection connection, BrokerMessage message, Queue<byte[]> queue) throws IOException, ClassNotFoundException {

		// Received from publisher the video chunks 3
		for (int hmc : message.videoTransfer.getChunkCounter()) {

			for (int ii = 0; ii < hmc; ii++) {
				connection.out.writeObject(message);
				connection.out.flush();
				message = (BrokerMessage) connection.in.readObject();
				queue.add(message.videoTransfer.getChunk());
			}
		}
	}
	@Override
	public void filterConsumers(ConsumerMessage message ,Set<PublisherInfo> listOfPublisherForThatHashtagWithoutMe) {
		boolean ifAmIInlist = false;
		for(PublisherInfo pp : listOfPublisherForThatHashtagWithoutMe) {
			if (message.getChannelName().equals(pp.channelName)) {
				ifAmIInlist = true;
			}
		}
		if (ifAmIInlist) {
			message.howManyPublisherHaveTheHashtag = listOfPublisherForThatHashtagWithoutMe.size() - 1;
		}
		else {
			message.howManyPublisherHaveTheHashtag = listOfPublisherForThatHashtagWithoutMe.size();
		}
	}
	@Override
	public List<BrokerImpl> getBroker() {
		return brokers;
	}
	@Override
	public void disconnect() {
		try {
			providerSocket.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	public ArrayList<BrokerImpl> updateForAllBrokers() throws IOException {
		Properties prop=new Properties();
		FileInputStream cf= new FileInputStream("C:\\distributed-systems\\config.properties");
		prop.load(cf);

		ArrayList<BrokerImpl> listOfAllBrokers = new ArrayList<BrokerImpl>();
		String ip = null;
		int port = 0;
		String name = null;
		for(int i = 1; i <= 3; i++) {
			ip = prop.getProperty("broker" + i + "ip");
			port = Integer.parseInt((prop.getProperty("broker" + i + "port")));
			name = prop.getProperty("broker" + i + "name");

			listOfAllBrokers.add(new BrokerImpl(ip, port, name));
		}
		return listOfAllBrokers;
	}
	public static void main(String args[]) throws IOException {

		Properties prop=new Properties();
		FileInputStream cf= new FileInputStream("C:\\distributed-systems\\config.properties");
		prop.load(cf);

		String ip = null;
		int port = 0;
		String name = null;
		switch (Integer.parseInt(args[0])) {
			case 1:
				ip = prop.getProperty("broker1ip");
				port = Integer.parseInt((prop.getProperty("broker1port")));
				name = prop.getProperty("broker1name");
				break;
			case 2:
				ip = prop.getProperty("broker2ip");
				port = Integer.parseInt((prop.getProperty("broker2port")));
				name = prop.getProperty("broker2name");
				break;
			case 3:
				ip = prop.getProperty("broker3ip");
				port = Integer.parseInt((prop.getProperty("broker3port")));
				name = prop.getProperty("broker3name");
				break;
		}

		BrokerImpl broker = new BrokerImpl(ip, port, name);

//		File file = new File("C:\\distributed-systems\\data\\brokersInfo.txt");
//		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
//		fw.write(broker.ip + " " + broker.port + "\n");
//		fw.close();

		broker.init(broker.port);
		broker.connect();

	}
}
