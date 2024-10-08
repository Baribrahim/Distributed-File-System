import java.io.IOException;
import java.net.Socket;

public class ControllerLogger extends Logger {

  private static final String LOG_FILE_SUFFIX = "Controller";

  @Override
  protected String getLogFileSuffix() {
    return LOG_FILE_SUFFIX;
  }

  private static ControllerLogger instance = null;

  public static void init(LoggingType loggingType) throws IOException {
    if (instance == null)
      instance = new ControllerLogger(loggingType);
    else
      throw new IOException("ControllerLogger has been already initialised");
  }

  public static ControllerLogger getInstance() {
    if (instance == null)
      throw new RuntimeException("ControllerLogger has not been initialised yet");
    return instance;
  }

  protected ControllerLogger(LoggingType loggingType) throws IOException {
    super(loggingType);
  }

  public void dstoreJoined(Socket socket, int dstorePort) {
    log("[New Dstore " + dstorePort + " " + socket.getLocalPort() + "<-" + socket.getPort() + "]");
  }
}
