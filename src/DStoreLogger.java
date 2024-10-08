import java.io.IOException;

public class DStoreLogger extends Logger {

  private static final String LOG_FILE_SUFFIX = "Dstore";

  private static DStoreLogger instance = null;

  private final String logFileSuffix;

  public static void init(LoggingType loggingType, int port) throws IOException {
    if (instance == null)
      instance = new DStoreLogger(loggingType, port);
    else
      throw new IOException("DstoreLogger already initialised");
  }

  public static DStoreLogger getInstance() {
    if (instance == null)
      throw new RuntimeException("DstoreLogger has not been initialised yet");
    return instance;
  }

  protected DStoreLogger(LoggingType loggingType, int port) throws IOException {
    super(loggingType);
    logFileSuffix = LOG_FILE_SUFFIX + "_" + port;
  }

  @Override
  protected String getLogFileSuffix() {
    return null;
  }
}
