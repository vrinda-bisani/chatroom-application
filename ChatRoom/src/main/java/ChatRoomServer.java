import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ChatRoomServer is a class responsible for creating a new server and starting the ChatRoomProtocol service.
 */
public class ChatRoomServer {
  private ServerSocket server;
  private ConcurrentMap<String, ClientInterface> clients;
  private Integer port;

  /**
   * Server constructor
   */
  public ChatRoomServer(){
    this.clients = new ConcurrentHashMap<>();
  }

  /**
   * Starts the server thread
   * @param port represented as int, port number
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  public void run(int port) throws IOException {
    try {
      this.server = new ServerSocket(port);
      this.port = this.server.getLocalPort();
      System.out.println("Server connected to "+this.port);
      while(true){
        Socket client = server.accept();
        ClientInterface handler = new ClientInterface(client, this);
        Thread ch = new Thread(handler);
        ch.start();
      }

    }
    catch (IOException e) {
      this.server.close();
      throw new RuntimeException(e);
    }

  }

  /**
   * This public method is used to get clients map
   *
   * @return represented as ConcurrentMap where key is String,username of client and value is object of ClientInterface
   */
  public ConcurrentMap<String, ClientInterface> getClients() {
    return this.clients;
  }

  /**
   * This public method is used to check if username is present in client map
   *
   * @param username represented as String, username
   * @return represented as boolean
   */
  public boolean hasClient(String username) {
    return this.clients.containsKey(username);
  }

  /**
   * Returns number of connected clients
   * @return number of connected clients
   */
  public Integer clientCount() {
    return this.clients.size();
  }

  /**
   * Enter point to start the server
   * @param args user args (none expected)
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  public static void main(String[] args) throws IOException {
    ChatRoomServer s = new ChatRoomServer();
    if(args.length == 1) {
      s.run(Integer.parseInt(args[0]));
    }
    else {
      s.run(0);
    }
  }
}
