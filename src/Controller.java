import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Controller {

  private static int cport;
  private static int r;
  private static int timeout;
  private static int rebalance_period;

  private static final Set<Index> stored_files = new HashSet<>();
  private static final Map<Integer, Socket> dstores = new HashMap<>();

  private static final Map<Socket, List<Socket>> clientLoadDstores = new HashMap<>();
  private static final Map<Socket, List<Socket>> clientReloadDstores = new HashMap<>();
  private static ControllerLogger controller_logger;
  private static ServerSocket controller_socket;

}
