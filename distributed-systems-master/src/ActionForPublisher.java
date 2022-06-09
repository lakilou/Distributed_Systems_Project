import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ActionForPublisher extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;

    public ActionForPublisher(Socket connection) throws IOException {
        this.in = new ObjectInputStream(connection.getInputStream());
        this.out = new ObjectOutputStream(connection.getOutputStream());
    }

    public void run() {
        try {
            Message message = (Message)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
