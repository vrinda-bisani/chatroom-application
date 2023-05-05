import assignment4.problem1.Grammar;
import assignment4.problem1.JSONFileParser;
import assignment4.problem1.SentenceGenerator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Converts client data packet readable to server, performs tasks accordingly and send response to client if needed.
 */
public class ChatRoomProtocol {
  /**
   * Constant for insult grammar file
   */
  public static final String GRAMMAR_JSON = "src/main/resources/insult_grammar.json";
  private String username;
  private DataInputStream in;
  private final DataOutputStream out;
  private ChatRoomServer server;

  /**
   * Only up to 10 clients can be connected in a chat room at one time
   */
  private static final Integer MAX_CLIENTS_ALLOWED = 10;

  /**
   * Chatroom protocol constructor
   * @param in server data input stream
   * @param out server data output stream
   * @param server server object
   */
  public ChatRoomProtocol(DataInputStream in, DataOutputStream out, ChatRoomServer server) {
    this.username = null;
    this.in = in;
    this.out = out;
    this.server = server;
  }

  /**
   * Processes the client request
   * @param messageIdentifier message identifier
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
   public void processInput(Integer messageIdentifier) throws IOException {
     int usernameLength = this.in.readInt();
     String user = bytesToString(this.in.readNBytes(usernameLength));
    switch (messageIdentifier){
      case MessageIdentifier.CONNECT_MESSAGE: synchronized (this.out) {this.login(user);}break;
      case MessageIdentifier.BROADCAST_MESSAGE: synchronized (this.out) {this.broadcast(user);}break;
      case MessageIdentifier.QUERY_CONNECTED_USERS: synchronized (this.out) {this.queryResponse(user);}break;
      case MessageIdentifier.DIRECT_MESSAGE: synchronized (this.out) {this.directMessage(user);}break;
      case MessageIdentifier.DISCONNECT_MESSAGE: synchronized (this.out) {this.logoff(user);}break;
      case MessageIdentifier.SEND_INSULT: synchronized (this.out) {this.sendInsult(user);}break;
      default:  throw new IllegalStateException("Unexpected value: " + messageIdentifier);
    }
  }

  /**
   * this private method is used to send insult message
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void sendInsult(String user) throws IOException {
    String message;
    try {
      if (!this.server.hasClient(user)) {
        this.failedMessage("sender: " + user + " doesn't exist.");
      }
      String target = bytesToString(this.in.readNBytes(this.in.readInt()));
      if (!this.server.hasClient(target)) {
        this.failedMessage("Receiver: " + target + " doesn't exist.");
      } else {
        String insult = this.createInsult();
        message = target + " " + insult;
        ClientInterface recipient = this.server.getClients().get(target);
        recipient.getDataOutputStream().writeInt(MessageIdentifier.SEND_INSULT);
        recipient.getDataOutputStream().writeInt(user.length());
        recipient.getDataOutputStream().writeBytes(user);
        recipient.getDataOutputStream().writeInt(target.length());
        recipient.getDataOutputStream().writeBytes(target);
        recipient.getDataOutputStream().writeInt(message.length());
        recipient.getDataOutputStream().writeBytes(message);

      }
    } catch (IOException e) {
      this.failedMessage("The format of SEND_INSULT is incorrect.");
    }
  }

  /**
   * This private method creates insult using assignment4 jar
   * @return represented as String, random insult
   */
  private String createInsult() {
    String insultMessage = "";
    JSONFileParser jsonFileParser = new JSONFileParser(GRAMMAR_JSON);
    Grammar insult = jsonFileParser.processJsonFile();
    SentenceGenerator generator = new SentenceGenerator(insult, null);
    insultMessage = generator.generateRandomSentence();
    return insultMessage;
  }

  /**
   * sends a CONNECT_RESPONSE to the client
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void login(String user) throws IOException {
    Boolean success = Boolean.FALSE;
    String message;
    if(this.server.hasClient(user)){
      this.out.writeInt(MessageIdentifier.CONNECT_RESPONSE);
      message = "Username already exist";
    }
    else if (this.server.getClients().size() == MAX_CLIENTS_ALLOWED) {
      this.out.writeInt(MessageIdentifier.DISCONNECT_RESPONSE);
      message = "Chatroom is full. Please try later";
      this.out.writeBoolean(Boolean.TRUE);
      this.out.writeInt(message.length());
      this.out.writeBytes(message);
    }
    else{
      this.out.writeInt(MessageIdentifier.CONNECT_RESPONSE);
      this.username = user;
      success = Boolean.TRUE;
      message = "There are " + this.server.clientCount() + " other connected clients";
    }
    this.out.writeBoolean(success);
    this.out.writeInt(message.length());
    this.out.writeBytes(message);
  }

  /**
   * sends a BROADCAST_MESSAGE to all connected users
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void broadcast(String user) throws IOException {
    int msgLen = this.in.readInt();
    byte[] msg = this.in.readNBytes(msgLen);
    if(!this.server.hasClient(user)){
      this.failedMessage("You are not authorized to send messages");
    }
    else {
      for (ClientInterface client : this.server.getClients().values()) {
        client.getDataOutputStream().writeInt(MessageIdentifier.BROADCAST_MESSAGE);
        client.getDataOutputStream().writeInt(user.length());
        client.getDataOutputStream().writeBytes(user);
        client.getDataOutputStream().writeInt(msgLen);
        client.getDataOutputStream().write(msg);
      }
    }
  }

  /**
   * sends a DIRECT_MESSAGE to the recipient
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void directMessage(String user) throws IOException {
    int recipientLength = this.in.readInt();
    String recipientName = bytesToString(this.in.readNBytes(recipientLength));
    int msgSize = this.in.readInt();
    String message = bytesToString(this.in.readNBytes(msgSize));
    if(!this.server.hasClient(user)){
      this.failedMessage("You are not authorized to send messages");
      return;
    }
    if(this.server.hasClient(recipientName)){
      ClientInterface recipient = this.server.getClients().get(recipientName);
      recipient.getDataOutputStream().writeInt(MessageIdentifier.DIRECT_MESSAGE);
      recipient.getDataOutputStream().writeInt(user.length());
      recipient.getDataOutputStream().writeBytes(user);
      recipient.getDataOutputStream().writeInt(recipientLength);
      recipient.getDataOutputStream().writeBytes(recipientName);
      recipient.getDataOutputStream().writeInt(msgSize);
      recipient.getDataOutputStream().writeBytes(message);
    }
    else{
      this.failedMessage("Recipient does not exists");
    }
  }

  /**
   * sends a FAILED_MESSAGE to the client
   * @param message failure message
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void failedMessage(String message) throws IOException {
    this.out.writeInt(MessageIdentifier.FAILED_MESSAGE);
    this.out.writeInt(message.length());
    this.out.writeBytes(message);
  }

  /**
   * sends a QUERY_USER_RESPONSE to the client
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void queryResponse(String user) throws IOException {
    int noOfUsers = 0;
    this.out.writeInt(MessageIdentifier.QUERY_USER_RESPONSE);
    if(!this.server.getClients().get(user).getSocket().isClosed()){
      noOfUsers = this.server.clientCount() - 1;
      this.out.writeInt(noOfUsers);
      if (noOfUsers > 0){
        for(String i: this.server.getClients().keySet()){
          if(i.equals(user)){
            continue;
          }
          this.out.writeInt(i.length());
          this.out.writeBytes(i);
        }
      }
    }
    else{
      this.out.writeInt(noOfUsers);
    }
  }

  /**
   * sends a DISCONNECT_RESPONSE to the client
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void logoff(String user) throws IOException {
    this.out.writeInt(MessageIdentifier.DISCONNECT_RESPONSE);
    String message;
    if(!this.server.hasClient(user) || this.server.getClients().get(user).getSocket().isClosed()){
      message = "Already disconnected!";
      this.out.writeBoolean(Boolean.FALSE);
      this.out.writeInt(message.length());
      this.out.writeBytes(message);
    }
    else{
      System.out.println(user + " left the chat!");
      message = "You are no longer connected";
      this.out.writeBoolean(Boolean.TRUE);
      this.out.writeInt(message.length());
      this.out.writeBytes(message);
      this.server.getClients().remove(user);
    }

  }

  /**
   * Returns username
   * @return username
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Converts byte array to string
   * @param str byte array of sting
   * @return string
   */
  private String bytesToString(byte[] str){
    return new String(str, StandardCharsets.UTF_8);
  }
}
