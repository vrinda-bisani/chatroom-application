import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * ClientInterface class implements Runnable interface which represents each client.
 * this class overrides run method and print in server terminal according to commands of that client and escalate identifier to protocol for processing input
 *
 * @author kabraambika19
 */
public class ClientInterface implements Runnable {
  private Socket socket;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private ChatRoomProtocol protocol;
  private ChatRoomServer server;

  /**
   * Constructor of ClientInterface with two provided parameters
   *
   * @param socket represented as Socket
   * @param server represented as ChatRoomServer, server for chat room
   */
  public ClientInterface(Socket socket, ChatRoomServer server) {
    try {
      this.socket = socket;
      this.server = server;
      this.dataInputStream = new DataInputStream(socket.getInputStream());
      this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
      this.protocol = new ChatRoomProtocol(dataInputStream, dataOutputStream, server);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This public method is used to get socket
   *
   * @return represented as Socket, this.socket
   */
  public Socket getSocket() {
    return this.socket;
  }

  /**
   * This public method is used to get dataInputStream
   *
   * @return represented as DataOutputStream, this.dataOutputStream
   */
  public DataOutputStream getDataOutputStream() {
    return this.dataOutputStream;
  }

  /**
   * This public method is used to get protocol
   *
   * @return represented as ChatRoomProtocol, this.protocol
   */
  public ChatRoomProtocol getProtocol() {
    return this.protocol;
  }

  @Override
  public void run() {
    //Process new connection in chatroom for a new client
    this.processNewConnect();

    //Once new connection setup, now process other commands
    while (!this.socket.isClosed()) {
      try {
        int msgIdentifier = this.dataInputStream.readInt();
        System.out.println("Client: " + protocol.getUsername() + ", requesting message identifier: " + msgIdentifier); //This will be printed in server terminal
        this.protocol.processInput(msgIdentifier);
      } catch (IOException e) {
        try {
          socket.close();
        } catch (IOException ex) {
          throw new RuntimeException(ex.getMessage());
        }
      }
    }
  }

  /**
   * This private method is used to process new connection with server for a new client
   */
  private void processNewConnect() {
    do {
      try {
        int msgIdentifier = this.dataInputStream.readInt();
        protocol.processInput(msgIdentifier);
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
    } while (protocol.getUsername() == null);

    this.server.getClients().put(protocol.getUsername(), this); // add user in server map
    System.out.println("New client " + protocol.getUsername() + " has joined the chat room!"); //this will be printed in server terminal
  }
}
