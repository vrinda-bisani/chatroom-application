import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ChatRoomClient is a class responsible for creating a new client by asking username and reading messages in chatroom.
 *
 */
public class ChatRoomClient {

  /**
   * Constant for help sign in terminal
   */
  private static final String HELP = "?";
  /**
   * Constant for login string
   */
  private static final String LOGIN_MESSAGE = "login";
  /**
   * Constant for two in validation of args length
   */
  private static final int TWO_ARGS = 2;
  private final Integer port;
  private final String hostName;
  private Socket clientSocket;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private String username;
  private BufferedReader bufferedReader;
  private MessageCodec messageCodec;

  /**
   * Constructor of ChatRoomClient with two parameters
   *
   * @param port represented as Integer, port number
   * @param hostName represented as host name
   */
  public ChatRoomClient(Integer port, String hostName) {
    this.port = port;
    this.hostName = hostName;
  }

  /**
   * This is a public method used to run client for read and write message in chatroom
   *
   * @param in represented as InputStream
   */
  public void startClient(InputStream in) {
    try {
      String readMessage;
      this.clientSocket = new Socket(hostName, port);
      this.dataInputStream = new DataInputStream(this.clientSocket.getInputStream());
      this.dataOutputStream = new DataOutputStream(this.clientSocket.getOutputStream());
      this.messageCodec = new MessageCodec(this.dataOutputStream);

      do {
        this.initiateUsername(in); //Ask for username from user and validate as if unique
      }
      while(!messageCodec.isConnected());

      this.startPrintThread(); // start a thread to print message in terminal for user

      System.out.println("Welcome to the chatroom, " + this.username + "! Type '?' to see for all commands.");

      while((readMessage = bufferedReader.readLine()) != null) {
        if(HELP.equals(readMessage)) {
          this.displayHelpMenu(); //print all commands in terminal for help
        }
        else {
          try {
            this.messageCodec.encodeMessage(readMessage); // encode terminal string to protocol
          }
          catch (IOException exception) {
            throw new RuntimeException(exception);
          }
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This private method is used to start a thread to print messages from other client in terminal
   */
  private void startPrintThread() {
    Thread printMsgThread = new Thread(() -> {
      while(this.messageCodec.isConnected()) {
        try {
          System.out.println(this.messageCodec.decodeMessage(dataInputStream));
        } catch (IOException e) {
          this.messageCodec.setConnected(Boolean.FALSE);
        }
      }
      this.closeResources();
    });
    printMsgThread.start();
  }

  /**
   * This private is used to close all open resources and exit from program
   */
  private void closeResources() {
    try {
      this.dataInputStream.close();
      this.dataOutputStream.close();
      this.clientSocket.close();
      System.exit(0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This private method is used to print help menu in terminal for user
   */
  private void displayHelpMenu() {
    System.out.println("""
        All available commands to interact in chatroom:
        logoff: sends a DISCONNECT_MESSAGE to the server
        who:    sends a QUERY_CONNECTED_USERS to the server
        @user:  sends a DIRECT_MESSAGE to the specified user to the server
        @all:   sends a BROADCAST_MESSAGE to the server, to be sent to all users connected
        !user:  sends a SEND_INSULT message to the server, to be sent to the specified user
        ?:      see all the available commands to interact in chatroom
        Example:
        SAMPLE                            Result
        @bob hello bob, how are you?      hello bob, how are you? is sent to user bob
        @all Hello Everyone!              Hello Everyone! sent to all connected clients
        !bob                              You are not good at this job! randomly generated insult
                                           sent to user bob                                                                         
        """);
  }

  /**
   * This private method is used to ask user for username in terminal and validate for unique name in chatroom
   *
   * @param in represented as InputStream
   * @throws IOException exception produced by failed or interrupted I/O operations while reading from bufferReader
   */
  private void initiateUsername(InputStream in) throws IOException {
      System.out.println("Enter your username for chatroom:");
      this.bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String name =  this.bufferedReader.readLine();
      this.username = name;
      this.messageCodec.setUsername(this.username);
      this.messageCodec.encodeMessage(LOGIN_MESSAGE);
      System.out.println(this.messageCodec.decodeMessage(this.dataInputStream));
  }

  /**
   * Entry point for client in chatroom
   *
   * @param args Array of String, two args for client, one is port number and hostname
   */
  public static void main(String[] args) {
    if(validateArgs(args)) {
      try{
        final int portNumber = Integer.parseInt(args[0]);
        final String hostName = args[1];
        ChatRoomClient client = new ChatRoomClient(portNumber, hostName);
        client.startClient(System.in);
      }
      catch(NumberFormatException ex) {
        throw new InvalidArgException(ex.getMessage());
      }
    }
    else {
      throw new InvalidArgException("Provide two args for running client, first one is port number and second one is host name!");
    }
  }

  /**
   * private method is used to validate length of args array
   * @param args Array of String, two args for client, one is port number and hostname
   * @return represented as boolean, true if args length is two
   */
  private static boolean validateArgs(String[] args) {
    return args.length >= TWO_ARGS;
  }
}
