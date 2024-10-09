import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
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

  public static void main(String[] args) {

    // Check the required number of arguments to prevent ArrayIndexOutOfBoundsException
    if (args.length < 4) {
      System.out.println("Insufficient arguments provided.");
      System.out.println("Expected: java Controller cport R timeout rebalancePeriod");
      return;
    }

    try {
      //Parse arguments from the command line input
      cport = Integer.parseInt(args[0]); //The controller port number
      r = Integer.parseInt(args[1]); //The replication factor, R
      timeout = Integer.parseInt(args[2]); //Timeout duration in milliseconds
      rebalance_period = Integer.parseInt(args[3]); //The period for rebalancing in milliseconds
    } catch (NumberFormatException e) {
      //Handle the case where any of the arguments are not a valid integer
      System.out.println("Unable to parse arguments: " + e.getMessage());
      System.out.println("All arguments must be integers. Expected format: java Controller cport R timeout rebalancePeriod");
      e.printStackTrace();
      return;
    }

    try {
      // Initialising the Logger for the Controller
      ControllerLogger.init(Logger.LoggingType.ON_FILE_AND_TERMINAL);
      System.out.println("The Controller's Logger has been initialised");
      controller_logger = ControllerLogger.getInstance();
      controller_logger.log("Controller initiated, Listening on port " + cport + "; Replication Factor: " + r + "; Timeout: " + timeout + "ms; Rebalance Period: " + rebalance_period
          + "ms");
    } catch (IOException e) {
      System.out.println("Unable to initialise the Controller's Logger");
      e.printStackTrace();
      return;
    }

    try {
      controller_socket = new ServerSocket(cport);
      //Infinite loop to keep the server running and listening for requests
      while (true) listen();
    } catch (IOException e) {
      log("Unable to bind to Controller Port: " + cport);
      e.printStackTrace();
    }
  }

  /**
   * Listens for incoming client connections and handles each connection in a separate thread.
   */
  private static void listen() {
    try {
      final Socket requestSocket = controller_socket.accept();
      Thread requestThread = new Thread(() -> handleRequests(requestSocket));
      requestThread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
    /**
     * This method reads lines of text from the client, logs each message, and parses commands contained in those lines.
     *
     * @param requestSocket The socket through which the client is connected.
     * @throws IOException If an I/O error occurs when sending the data through the socket.
     */
    private static void handleClientMessages(Socket requestSocket) {
      try {
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(requestSocket.getInputStream()));
        String line;
        while ((line = clientReader.readLine()) != null) {
          controller_logger.messageReceived(requestSocket, line);
          Scanner scanner = new Scanner(line);
          String command = scanner.next();
          handleCommand(command, scanner, requestSocket);
        }
      } catch (IOException e) {
        handleDstoreCrash(requestSocket);
      }
    }


  /**
   * Cleans up the connection by removing the socket from the list of data stores
   *
   * @param requestSocket The socket through which the client is connected.
   */
  private static void cleanupConnection(Socket requestSocket) {
    synchronized (dstores) {
      removeDstore(requestSocket);
    }
    log("Socket " + requestSocket.getRemoteSocketAddress() + " closed");
  }

  /**
   * Handles requests from a client connected through the given socket.
   *
   * @param requestSocket The socket through which the client is connected.
   */
  private static void handleRequests(Socket requestSocket) {
    handleClientMessages(requestSocket);
  }

  /**
   * Parses and executes a command received from a client.
   * This method applies a timeout, processes the command based on a protocol,
   * and sets the socket timeout settings before and after command processing.
   *
   * @param command The command to be executed, parsed from the client input.
   * @param parameterScanner A Scanner to read additional parameters for the command.
   * @param requestSocket The client socket from which the command was received.
   * @throws IOException If an I/O error occurs when sending the data through the socket.
   */
  private static void handleCommand(String command, Scanner parameterScanner, Socket requestSocket) throws IOException {
    //Set a timeout on the socket
    requestSocket.setSoTimeout(timeout);
    try (parameterScanner) {
      if (command.equals(Protocol.JOIN_TOKEN)) {
        // If the command is JOIN from Dstore, add a Dstore using the next integer parameter from the scanner
        handleJOIN(requestSocket, parameterScanner.nextInt());
      }
    } catch (NoSuchElementException e) {
      //Log and handle the case where the necessary parameters are missing or in wrong format
      log("Invalid request format");
      e.printStackTrace();
    } finally {
      //Reset the timeout to zero
      requestSocket.setSoTimeout(0);
    }
  }

  private static void handleDstoreCrash(Socket socket) {
    if (getDstorePort(socket) != -1) {
      dstores.values().remove(socket);
      for (Index file : stored_files) {
        if (file.dstores.isEmpty()) {
          stored_files.remove(file);
        }
        file.removeDstore(socket);
      }
    }
  }

  /**
   * Adds a new Dstore, this function is called when the command JOIN is received.
   *
   * @param socket The socket through which the Dstore is connected.
   * @param port The port number associated with the Dstore's socket.
   */
  private static void handleJOIN(Socket socket, int port) {
    synchronized (dstores) {
      //Add the Dstore to the dstore HashMap if it's not already present, mapped by its port
      dstores.putIfAbsent(port, socket);
    }
    controller_logger.dstoreJoined(socket, port);

    // Add a new thread to monitor the Dstore connection
    new Thread(() -> {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (reader.readLine() != null) {
          // Do nothing, just keep the connection alive
        }
      } catch (IOException e) {
        // Connection lost or Dstore crashed
        synchronized (dstores) {
          dstores.remove(port);
        }
        log("Dstore " + port + " disconnected or crashed");
      }
    }).start();
  }

  private static int getDstorePort(Socket dstoreSocket) {
    for (Map.Entry<Integer, Socket> entry : dstores.entrySet()) {
      if (entry.getValue().equals(dstoreSocket)) {
        return entry.getKey();
      }
    }
    return -1; // Return -1 if the Dstore's endpoint port is not found
  }

  /**
   * Removes a Dstore connection from the storage system based on its socket.
   *
   * @param socketToRemove The socket through which the Dstore is connected and which needs to be removed.
   */
  private static void removeDstore(Socket socketToRemove) {
    dstores.values().remove(socketToRemove);
  }

  /**
   * Logs a message with a timestamp.
   *
   * @param message The message to log.
   */
  private static void log(String message) {
    controller_logger.log("[" + Instant.now() + "] " + message);
  }


}
