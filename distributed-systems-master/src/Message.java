import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public abstract class Message implements Serializable {
    private static final long serialVersionUID = -2723363051271966964L;
    protected String channelName;
    protected String ip;
    protected int port;

}