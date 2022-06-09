import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class AppNodeImpl extends Thread implements AppNode  {

    //static int counterForPort = 14179; // consumer 22222
    String ip;
    int port;
    String ipForTheFirstBroker;
    int portForTheFirstBroker;
    ChannelName channelName;
    BigInteger hashAppNode;
    String path;
    Message message;
    ArrayList<BrokerImpl> allBrokers = new ArrayList<BrokerImpl>();
    ArrayList<AppNodeImpl> followers = new ArrayList<AppNodeImpl>();
    ArrayList<AppNodeImpl> following = new ArrayList<AppNodeImpl>();
    Set<BrokerImpl> myBrokers = new HashSet<BrokerImpl>();
    ArrayList<VideoFile> downloadedVideos = new ArrayList<VideoFile>();
    ServerSocket providerSocket;
    Socket connection = null;
    String publisherOrConsumer;

    public AppNodeImpl(ChannelName channelName, String ipBroker, int portBroker, String pOc, int port) throws UnknownHostException {
        this.ip = InetAddress.getLocalHost().getHostAddress();
        this.port = port;
        this.channelName = channelName;
        path = "C:\\distributed-systems\\data\\" + channelName.channelName;
        hashAppNode = SHA1.hashText(channelName.channelName);
        this.ipForTheFirstBroker = ipBroker;
        this.portForTheFirstBroker = portBroker;
        this.publisherOrConsumer = pOc;
    }
    @Override
    public void notifyBrokerForHashtagsOrChannelName(Connection con, String hashTagOrChannelName, BrokerImpl b) throws IOException, InterruptedException {
        message = new PublisherMessage(hashTagOrChannelName, this.channelName.channelName, this.ip, this.port);
        con.out.writeObject(message);
        con.out.flush();
        myBrokers.add(b);
    }
    public BrokerImpl findTheRightBroker(BigInteger hashS) throws IOException {
        int size = allBrokers.size();
        int count=0;
        for(BrokerImpl b: allBrokers) {
            count++;
            if ((hashS.compareTo(b.brokerHash) <= 0)) {
                return b;
            }
            else if ((hashS.compareTo(b.brokerHash) == 1 && count<size)){
                continue;
            }
            else{
                BigInteger modValue = BigInteger.valueOf(size);
                BigInteger indexOfBr = hashS.mod(modValue);
                return allBrokers.get(indexOfBr.intValue());
            }
        }
        return null;
    }
    public void initBrokers() throws IOException, ClassNotFoundException {

        message = new AppNodeMessage();
        Connection giveMeTheListOfBroker = new Connection(new Socket(ipForTheFirstBroker, portForTheFirstBroker));
        giveMeTheListOfBroker.out.writeObject(message);
        giveMeTheListOfBroker.out.flush();
        AppNodeMessage m = (AppNodeMessage) giveMeTheListOfBroker.in.readObject();
        allBrokers = m.getAllBrokers();
        giveMeTheListOfBroker.in.close();
        giveMeTheListOfBroker.out.close();
    }
    public void initVideos() throws IOException, TikaException, SAXException {
        File file = new File(path + "\\allVideos.txt");
        Scanner input = new Scanner(file);
        List<String> listOfVideos = new ArrayList<String>();
        while (input.hasNextLine()) {
            listOfVideos.add(input.nextLine());
        }
        VideoFile v;
        for(String videoName: listOfVideos) {
            v = new VideoFile(videoName, this.channelName.channelName, path);
            this.channelName.updateHashMap(v);
        }
    }
    @Override
    public void run() {

        try {
            int nothing = 0;
            this.init(nothing);
            this.connect();

        } catch (IOException | ClassNotFoundException | TikaException | SAXException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void init(int a) throws IOException, ClassNotFoundException, TikaException, SAXException {
        this.initBrokers();
        Collections.sort(allBrokers);
        providerSocket = new ServerSocket(port, 10);
    }
    public void connect() throws IOException, ClassNotFoundException, TikaException, SAXException, InterruptedException {
        if (this.publisherOrConsumer.equals("publisher")) {
            this.connectPublisher();
        }
        else {
            this.connectConsumer();
        }
    }
    public void connectPublisher() throws IOException, TikaException, SAXException, InterruptedException {
        BrokerImpl connectedBroker;
        Connection con;
        this.initVideos();
//        System.out.println(this.channelName.userVideoFilesMap.get("a"));
        try {
            connectedBroker = this.findTheRightBroker(SHA1.hashText(this.channelName.channelName));
            con = new Connection(new Socket(connectedBroker.ip, connectedBroker.port));
            this.notifyBrokerForHashtagsOrChannelName(con, this.channelName.channelName, connectedBroker); // sent channelName

            for (String hashString : this.channelName.hashtagsPublished) { // sent each hashtags
                connectedBroker = this.findTheRightBroker(SHA1.hashText(hashString));
                con = new Connection(new Socket(connectedBroker.ip, connectedBroker.port));
                this.notifyBrokerForHashtagsOrChannelName(con, hashString, connectedBroker);
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | InterruptedException ioException) {
            ioException.printStackTrace();
        }
        System.out.println(channelName.channelName + ": I am waiting for requests");
        while (true) {
            connection = providerSocket.accept();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection connectWithBrokerForConsumer = new Connection();
                    BrokerMessage messageFromBroker;
                    try {
                        connectWithBrokerForConsumer.in = new ObjectInputStream(connection.getInputStream());
                        connectWithBrokerForConsumer.out = new ObjectOutputStream(connection.getOutputStream());

                        // received hashtag for the search 1
                        messageFromBroker = (BrokerMessage) connectWithBrokerForConsumer.in.readObject();
//                        System.out.println("I received message for this hashtag: " + messageFromBroker.getSearch());
                        // received hashtag for the search 1

                        // Preparing BrokerMessage.chunkCounter, BrokerMessage.videoNames
                        if (!messageFromBroker.getSearch().equals(channelName.channelName)) { // for the hashtag
                            preparePushForHashtag(messageFromBroker);

                            // Send back chunkCounter and videoNames 2.1
                            connectWithBrokerForConsumer.out.writeObject(messageFromBroker);
                            connectWithBrokerForConsumer.out.flush();

                            // Sent to broker the video chunks 3.1
                            push(connectWithBrokerForConsumer, messageFromBroker, channelName.userVideoFilesMap.get(messageFromBroker.getSearch()));

                        }
                        else { // for the channelName
                            // Preparing BrokerMessage.chunkCounter, BrokerMessage.videoNames
                            ArrayList<VideoFile> setVideoFile = new ArrayList<VideoFile>();
                            preparePushForChannelName(messageFromBroker,setVideoFile);

                            // Send back chunkCounter and videoNames 2.2
                            connectWithBrokerForConsumer.out.writeObject(messageFromBroker);
                            connectWithBrokerForConsumer.out.flush();

                            // Sent to broker the video chunks 3.2
                            push(connectWithBrokerForConsumer, messageFromBroker, setVideoFile);
                        }
                        // End of sent to broker the video chunks 3
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            connectWithBrokerForConsumer.in.close();
                            connectWithBrokerForConsumer.out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.start();
            //t.join();
        }
    }
    public void connectConsumer() throws IOException, ClassNotFoundException, TikaException, SAXException {
        BrokerImpl connectedBroker;
        Connection con;
        Message messageForBroker;
        String register;
        String search;

        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.print(this.channelName.channelName + "-> Search hashtag or channelName or press -end- for exit: ");
        search = myObj.nextLine();  // Read user input


        while(!search.equals("-end-")) {

            connectedBroker = this.findTheRightBroker(SHA1.hashText(search));
            con = new Connection(new Socket(connectedBroker.ip, connectedBroker.port));
            System.out.print(this.channelName.channelName + "-> Do you want to register at " + search + "? :");
            register = myObj.nextLine();  // Read user input

            // Send message for search hashtag 1
            messageForBroker = new ConsumerMessage(this.channelName.channelName, search, this.ip, this.port);
            ((ConsumerMessage)messageForBroker).register = register;
            con.out.writeObject(messageForBroker);
            con.out.flush();

            // Received from broker howManyPublisherHaveTheHashtag, chunkCounter and videoNames 2
            ConsumerMessage consumerMessageForBroker = (ConsumerMessage) con.in.readObject();
            if(consumerMessageForBroker.findIt) {
                int x = 0;
                for (int publishers = 0; publishers < consumerMessageForBroker.howManyPublisherHaveTheHashtag; publishers++) {
                    for (int hmc : consumerMessageForBroker.videoTransfer.getChunkCounter()) {
                        ArrayList<byte[]> listOfChunks = new ArrayList<byte[]>();
                        for (int i = 0; i < hmc; i++) {
                            con.out.writeObject(consumerMessageForBroker);
                            con.out.flush();
                            consumerMessageForBroker = (ConsumerMessage) con.in.readObject();
                            listOfChunks.add(consumerMessageForBroker.videoTransfer.getChunk());
                        }
                        String path = VideoFile.byteArrayToFile(VideoFile.toByteArray(listOfChunks), this.channelName.channelName, consumerMessageForBroker.videoTransfer.getVideoNames().get(x));
                        VideoFile newVid = new VideoFile(consumerMessageForBroker.getSearch(), consumerMessageForBroker.videoTransfer.getAssociatedHashTags(), path);
                        downloadedVideos.add(newVid);
                        x++;
                    }
                }
                System.out.println("Download completed. Enjoy the videos!");
            }
            else {
                System.out.println(consumerMessageForBroker.notFound);
            }
            con.out.close();
            con.in.close();
            myObj = new Scanner(System.in);  // Create a Scanner object
            System.out.print(this.channelName.channelName + "-> Search hashtag or channelName or press -end- for exit: ");
            search = myObj.nextLine();  // Read user input
        }
        while (true) {
            System.out.println( this.channelName.channelName + "I am waiting");
            connection = providerSocket.accept();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Connection connectWithBrokerForConsumer = new Connection();
                    BrokerMessage messageFromBroker;
                    try {
                        connectWithBrokerForConsumer.out = new ObjectOutputStream(connection.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
// USED           ------------------------------------
    @Override
    public void preparePushForHashtag(BrokerMessage message) {
        for (VideoFile vv : channelName.userVideoFilesMap.get(message.getSearch())) {
            message.videoTransfer.getChunkCounter().add(vv.videoChunks.size());
            message.videoTransfer.getVideoNames().add(vv.videoName);
        }
    }
    @Override
    public void preparePushForChannelName(BrokerMessage message, ArrayList<VideoFile> listOfVideoFile) {
        for(String hashtag: channelName.userVideoFilesMap.keySet()){
            for(VideoFile vvv: channelName.userVideoFilesMap.get(hashtag)) {
                if (!message.videoTransfer.getVideoNames().contains(vvv.videoName)) {
                    message.videoTransfer.getChunkCounter().add(vvv.videoChunks.size());
                    message.videoTransfer.getVideoNames().add(vvv.videoName);
                    listOfVideoFile.add(vvv);
                }
            }
        }
    }
    @Override
    public void push(Connection connection, BrokerMessage message, ArrayList<VideoFile> listOfVideoFile) throws IOException, ClassNotFoundException {
        for(VideoFile vvv: listOfVideoFile) {
            for (byte[] bb : vvv.videoChunks) {
                message = (BrokerMessage) connection.in.readObject();
                message.videoTransfer.setChunk(bb);
                connection.out.writeObject(message);
                connection.out.flush();
            }
        }
    }
    @Override
    public void register(String s) {
    }
    @Override
    public void addHashTag(String hashTag) throws IOException {
    }
    @Override
    public void removeHashTag(String hashtag) {
    }
    @Override
    public List<BrokerImpl> getBroker() {
        return allBrokers;
    }
    @Override
    public Set<BrokerImpl> getBrokerList() {
        return myBrokers;
    }
    @Override
    public void disconnect() throws IOException {
        System.out.println("I am closing the connection. BYE!");
    }
    @Override
    public void playData(String path, ArrayList<byte []> v) throws IOException {
    }
// MAYBER USED    ---------------------------------
    @Override
    public BrokerImpl getRandomElement(ArrayList<BrokerImpl> list) {
        int size =list.size();
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }
// MAIN    ---------------------------------
    public static void main(String args[]) throws UnknownHostException {
        String ipBroker = args[0];
        int portBroker = Integer.parseInt(args[1]);

        AppNodeImpl publisher1 = new AppNodeImpl(new ChannelName("Lakilou"), ipBroker, portBroker, "publisher", 21255);
        publisher1.start();
//        AppNodeImpl publisher2 = new AppNodeImpl(new ChannelName("Nikos"), ipBroker, portBroker, "consumer", 33384);
//        publisher2.start();
//        AppNodeImpl publisher3 = new AppNodeImpl(new ChannelName("Giannis"), ipBroker, portBroker, "consumer", 11458);
//        publisher3.start();

    }
}
