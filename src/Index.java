import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Index to keep track of stored files
 */
public class Index {

  enum Status {
    STORE_IN_PROGRESS,
    STORE_COMPLETE,
    REMOVE_IN_PROGRESS,
    REMOVE_COMPLETE
  }

  final String fileName;
  final String fileSize;
  final List<Socket> dstores; //List of Dstores where the file is stored
  final List<Socket> blacklistedDstores; //List of Dstores that are blacklisted for this file
  final Socket storageLocation; //Socket representing where the file is stored
  final Object syncLock; //Object for synchronization


  Status status;
  int acks;

  final List<Socket> loadingClients;

  Index(String name, String size, Status status, Socket storedBy) {
    this.fileName = name;
    this.fileSize = size;
    this.dstores = new ArrayList<>();
    this.blacklistedDstores = new ArrayList<>();
    this.status = status;
    this.acks = 0;
    this.storageLocation = storedBy;
    this.syncLock = new Object();
    this.loadingClients = new ArrayList<>();
  }

  public void removeDstore(Socket socket) {
    dstores.remove(socket);
  }
  /**
   * Custom hash code method
   *
   * @return Hash code of the index entry based on its name
   */
  @Override
  public int hashCode() {
    return this.fileName.hashCode();
  }

  /**
   * Custom string representation of the index entry
   *
   * @return Name of the file
   */
  @Override
  public String toString() {
    return this.fileName;
  }

}
