import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChannelName implements Serializable {

	String channelName;
	Set<String> hashtagsPublished = new HashSet<String>();
	HashMap<String, ArrayList<VideoFile>> userVideoFilesMap = new HashMap<String, ArrayList<VideoFile>>();


	public ChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void updateHashMap(VideoFile v) {
		for (String hashtag : v.associatedHashtags) {
			hashtagsPublished.add(hashtag);
			if (!userVideoFilesMap.containsKey(hashtag)) {
				ArrayList<VideoFile> a = new ArrayList<VideoFile>();
				a.add(v);
				userVideoFilesMap.put(hashtag, a);
			} else {
				userVideoFilesMap.get(hashtag).add(v);
			}
		}
	}
}
