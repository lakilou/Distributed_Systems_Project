import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface Node extends Serializable {

	void init(int a) throws IOException, ClassNotFoundException, TikaException, SAXException;
	List<BrokerImpl> getBroker();
	void connect() throws IOException, ClassNotFoundException, TikaException, SAXException, InterruptedException;
	void disconnect() throws IOException;
}