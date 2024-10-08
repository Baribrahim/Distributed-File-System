import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public abstract class Logger {

  public enum LoggingType {
    NO_LOG, // No Log completely
    ON_TERMINAL_ONLY, // Log to Console only
    ON_FILE_ONLY, // Log to File only
    ON_FILE_AND_TERMINAL // Log to both Console and File
  }

  protected final LoggingType loggingType;
  protected PrintStream ps;

  /**
   * Constructor for Logger class
   *
   * @param loggingType The type of logging to perform (file only, terminal only, or both)
   */
  protected Logger(LoggingType loggingType) {
    this.loggingType = loggingType;
  }

  /**
   * Abstract method to be implemented by subclasses to define the suffix for log files
   *
   * @return A string representing the log file suffix.
   */
  protected abstract String getLogFileSuffix();

  /**
   * Provides a synchronized method to retrieve a PrintStream for logging.
   * Ensures that only one PrintStream instance is created per Logger instance.
   *
   * @return A PrintStream object configured to output to a file with a unique timestamp in its name.
   * @throws IOException If an I/O error occurs when opening the file.
   */
  protected synchronized PrintStream getPrintStream() throws IOException {
    if (ps == null)
      ps = new PrintStream(getLogFileSuffix() + "_" + System.currentTimeMillis() + ".log");
    return ps;
  }

  /**
   * Decides if logging should be performed to a file based on the logging type.
   *
   * @return True if the logger should write output to a file.
   */
  protected boolean logToFile() {
    return loggingType == LoggingType.ON_FILE_ONLY || loggingType == LoggingType.ON_FILE_AND_TERMINAL;
  }

  /**
   * Decides if logging should be performed to the terminal based on the logging type
   * @return True if the logger should write output to the terminal.
   */
  protected boolean logToTerminal() {
    return loggingType == LoggingType.ON_TERMINAL_ONLY || loggingType == LoggingType.ON_FILE_AND_TERMINAL;
  }

  /**
   * Logs a message to the file and/or terminal based on the logging type
   *
   * @param message The message to be logged.
   */
  protected void log(String message) {
    if (logToFile())
      try { getPrintStream().println(message); } catch(Exception e) { e.printStackTrace(); }
    if (logToTerminal())
      System.out.println(message);
  }

  /**
   * Logs a formatted message indicating a message was sent.
   *
   * @param socket The socket through which the message was sent.
   * @param message The actual message sent.
   */
  public void messageSent(Socket socket, String message) {
    log("[" + socket.getLocalPort() + "->" + socket.getPort() + "] " + message);
  }

  /**
   * Logs a formatted message indicating a message was received.
   *
   * @param socket The socket from which the message was received.
   * @param message The actual message received.
   */
  public void messageReceived(Socket socket, String message) {
    log("[" + socket.getLocalPort() + "<-" + socket.getPort() + "] " + message);
  }
}
