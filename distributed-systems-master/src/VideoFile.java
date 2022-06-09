import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;

import org.xml.sax.SAXException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class VideoFile implements Serializable {
	String videoName;
	String channelName;
	MetaData metadata;
	ArrayList<String> associatedHashtags;
	ArrayList<byte[]> videoChunks;
	String path;

	public VideoFile(String videoName, ArrayList<String> associatedHashtags, String path) throws TikaException, IOException, SAXException {
		this.videoName = videoName;
		this.associatedHashtags = associatedHashtags;
		this.path = path;
		this.extractMetaData(this.path);
	}
	public VideoFile(String videoName, String channelName, String path) throws IOException, TikaException, SAXException {
		this.videoName = videoName;
		this.channelName = channelName;
		this.path = path; // \\data\\channelName
		this.takeHashTags(this.path);
		//this.fileToByteArray(path + "\\" + videoName +".mp4");
		this.extractMetaData(this.path + "\\"  + videoName + ".mp4");
		videoChunks = this.generateChunks(path);
	}
	public void extractMetaData(String path) throws TikaException, SAXException, IOException {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(new File(path));
		ParseContext pcontext = new ParseContext();
		//Html parser
		MP4Parser MP4Parser = new MP4Parser();
		MP4Parser.parse(inputstream, handler, metadata,pcontext);
		String[] metadataNames = metadata.names();
		String dateCreated=metadata.get(metadataNames[1]);
		String frameHeight=metadata.get(metadataNames[3]);
		//den einai sgro oti edw prepei na mpei to framerate ta metadata to orizoun os audioSampleRate kai oxi os frameRate
		String framerate=metadata.get(metadataNames[8]);
		String frameWidth=metadata.get(metadataNames[11]);
		String length=metadata.get(metadataNames[12]);
		//To filetype tha to xreisimopoiisoume oste na min ginetai na ginei initialize ena file to opoio tha einai kati ektws apo Video/mp4
		String fileType=metadata.get(metadataNames[13]);
		this.metadata = new MetaData( dateCreated,  length,  framerate,  frameWidth,  frameHeight, fileType);
//		System.out.println(dateCreated +"   "+ length+"   "+framerate+"   "+frameWidth+"   "+frameHeight+"   "+fileType);

	}
	public void takeHashTags(String path) throws FileNotFoundException {
		this.associatedHashtags = new ArrayList<String>();
		File file = new File(path + "\\"  + videoName + ".txt");
		Scanner input = new Scanner(file);
		while (input.hasNextLine()) {
			this.associatedHashtags.add(input.nextLine());
		}
	}
	public ArrayList<byte []> generateChunks(String path) throws IOException {
		ArrayList<byte[]> listOfChunks = new ArrayList<byte[]>();
		File file = new File(path + "\\" + videoName + ".mp4");//File read from Source folder to Split.

		byte[] allBytes = Files.readAllBytes(file.toPath());
		int sizeOfChunk = 64000;
		int numberOfChunks = allBytes.length / sizeOfChunk;
		byte[] a;

		int counter = 0;
		for(int x = 0; x < numberOfChunks; x++) {
			a = new byte[sizeOfChunk];
			for (int y = 0; y < sizeOfChunk; y++) {
				a[y] = allBytes[counter];
				counter++;
			}
			listOfChunks.add(a);
		}
		return listOfChunks;
	}
	public ArrayList<byte []> generateChunks5(String path) {
		//aa einai auto pou epistrefete kai diatirei ta 20 chunks
		ArrayList<byte[]> aa = new ArrayList<byte[]>();
		//listOfChunks einai ena arraylist to opoio tha apothikeuei ta paths sta opoia diaspa o publisher
		//to video se 20 kommatia

		ArrayList<String> listOfChunks = new ArrayList<String>(4);

		try {
			File file = new File(path + "\\" + videoName + ".mp4");//File read from Source folder to Split.
			if (file.exists()) {

				String videoFileName = file.getName().substring(0, file.getName().lastIndexOf(".")); // Name of the videoFile without extension
				File splitFile = new File(path + "\\" + videoName);//Destination folder to save.
				if (!splitFile.exists()) {
					splitFile.mkdirs();
				}

				int i = 01;// Files count starts from 1
				InputStream inputStream = new FileInputStream(file);
				String videoFile = splitFile.getAbsolutePath() + "/" + String.format("%02d", i) + "_" + file.getName();// Location to save the files which are Split from the original file.
				OutputStream outputStream = new FileOutputStream(videoFile);
				listOfChunks.add(videoFile);//edw ginetai add to 01 prwto path
				System.out.println("File Created Location: " + videoFile);

				int totalPartsToSplit = 4;// Total files to split.
				int splitSize = inputStream.available() / totalPartsToSplit;
				int streamSize = 0;
				int read = 0;
				while ((inputStream.read()) != -1) {
					if (streamSize == splitSize) {
						if (i != totalPartsToSplit) {
							i++;
							String fileCount = String.format("%02d", i); // output will be 1 is 01, 2 is 02
							videoFile = splitFile.getAbsolutePath() + "/" + fileCount + "_" + file.getName();
							outputStream = new FileOutputStream(videoFile);
							listOfChunks.add(videoFile);//egw ginontai add ta paths 02-20
							System.out.println("File Created Location: " + videoFile);
							streamSize = 0;
						}
					}
					outputStream.write(read);

					streamSize++;
				}
				//listOfChunks.add(videoFile);

				inputStream.close();
				outputStream.close();
				int sum = 0, count = 0;
				for (String bb : listOfChunks) {

					if(bb!=null) {
						//Apo kathe path pou dimiourgisame ta 20 diaspasmena video
						//pairnoume ta bytes chunk tou kathe subvideo
						byte[] a = fileToByteArray1(bb);
						//add chunk ston pinaka pou epistrefoume
						aa.add(a);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return aa;
	}
	public static byte[] toByteArray(ArrayList<byte[]> bytesList) {
		int size = 0;
		for (byte[] bytes : bytesList) {
			size += bytes.length;
		}
		ByteBuffer byteBuffer = ByteBuffer.allocate(size);
		for (byte[] bytes : bytesList) {
			byteBuffer.put(bytes);
		}
		byte[] a = byteBuffer.array();
		return a;
	}
//	public void fileToByteArray(String path) throws IOException { //    data\\channelName
//		File file = new File(path);
//		this.videoFileChunk = Files.readAllBytes(file.toPath());
//	}
	public byte[] fileToByteArray1(String path) throws IOException {
		File file = new File(path);
		byte[] videoFileChunk= Files.readAllBytes(file.toPath());
		return videoFileChunk;
	}
	public static String byteArrayToFile(byte[] b, String channelName, String videoName) throws IOException {
		//Reverse byte[] array to File
		String path = "C:\\distributed-systems\\data\\" + channelName + "\\downloadedVideos";

		File splitFile = new File(path);//Destination folder to save.
		if (!splitFile.exists()) {
			splitFile.mkdirs();
			System.out.println("Directory Created -> " + splitFile.getAbsolutePath());
		}
		Path path1 = Paths.get(path + "\\" + videoName +  ".mp4");
		Files.write(path1, b);
		path = path + "\\" + videoName +  ".mp4";
		return path;
	}
//	public static void byteArrayToFile1(byte[] b, String s) throws IOException {
//			//Reverse byte[] array to File
//			Path path1 = Paths.get(s);
//			Files.write(path1, b);
//	}
}