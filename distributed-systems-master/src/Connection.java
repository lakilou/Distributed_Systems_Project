import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;

    public Connection() {
    }

    public Connection(Socket requestSocket) throws IOException {
        this.requestSocket = requestSocket;
        this.out = new ObjectOutputStream(this.requestSocket.getOutputStream());
        this.in = new  ObjectInputStream(this.requestSocket.getInputStream());
    }

}
