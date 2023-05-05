import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MessageCodec is a class used encode and decode message sent or received from other clients.
 *
 * @author kabraambika19
 */
public class MessageCodec {

  /**
   * Constant for string login command
   */
  private static final String LOGIN_COMMAND = "login";

  /**
   * Constant for string logoff command
   */
  private static final String LOGOFF_COMMAND = "logoff";

  /**
   * Constant for string who command
   */
  private static final String WHO_COMMAND = "who";

  /**
   * Constant for string user specific command
   */
  private static final String USER_SPEC_COMMAND = "@";

  /**
   * Constant for string all command
   */
  private static final String ALL_COMMAND = "@all";

  /**
   * Constant for string insult command
   */
  private static final String INSULT_COMMAND = "!";

  /**
   * Constant for regex for whitespace
   */
  private static final String WHITESPACE_REGEX = "\\s+";
  private String username;
  private DataOutputStream dataOutputStream;
  private boolean isConnected;

  /**
   * Constructor of MessageCodec
   *
   * @param dataOutputStream represented as DataOutputStream, write primitive data types to an output stream in a portable way
   */
  public MessageCodec(DataOutputStream dataOutputStream) {
    this.dataOutputStream = dataOutputStream;
  }

  /**
   * This getter method is used to check is MessageCodec is connected
   *
   * @return represented as boolean, this.isConnected
   */
  public boolean isConnected() {
    return this.isConnected;
  }

  /**
   * This setter method is used to set MessageCodec connected with username
   *
   * @param connected represented as boolean
   */
  public void setConnected(boolean connected) {
    this.isConnected = connected;
  }

  /**
   * This setter method is used to set username in MessageCodec class
   *
   * @param username represented as String, username/client username provided by user
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * This public method is used to encodeMessage and convert according to protocol
   *
   * @param msgFromUser represented as String, terminal command with message written by user
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  public void encodeMessage(String msgFromUser) throws IOException {
    int usernameLength = username.length();
    byte[] usernameToByte = this.convertStringToBytes(username);
    String[] msgTokens = msgFromUser.split(WHITESPACE_REGEX);
    if(this.validMsgToken(msgTokens)) {
      this.convertIntoProtocol(usernameLength, usernameToByte, msgTokens);
    }
  }

  /**
   * This private method checks for message token length which should be greater than zero
   *
   * @param msgTokens represented as Array of Strings, message
   * @return represented as boolean, array length should be greater than zero
   */
  private boolean validMsgToken(String[] msgTokens) {
    return msgTokens.length > 0;
  }

  /**
   * This private method converts string message into primitive data types and write in dataOutputStream
   *
   * @param usernameLength represented as int, username length
   * @param usernameToByte represented as Array of byte, username in bytes
   * @param msgTokens represented as Array of String, terminal command by user along with messages
   * @throws IOException exception produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void convertIntoProtocol(int usernameLength, byte[] usernameToByte, String[] msgTokens)
      throws IOException {
    String msgCommand = msgTokens[0];
    String receiverName = null;
    if(!msgCommand.equals(ALL_COMMAND) && msgCommand.startsWith(USER_SPEC_COMMAND)) {
      receiverName = msgCommand.substring(1);
      msgCommand = USER_SPEC_COMMAND;
    } else if (msgCommand.startsWith(INSULT_COMMAND)) {
      receiverName = msgCommand.substring(1);
      msgCommand = INSULT_COMMAND;
    }

    switch (msgCommand) {
      case LOGOFF_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.DISCONNECT_MESSAGE);
        this.addUserDetails(usernameLength, usernameToByte);
      }
      case WHO_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.QUERY_CONNECTED_USERS);
        this.addUserDetails(usernameLength, usernameToByte);
      }
      case USER_SPEC_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.DIRECT_MESSAGE);
        this.addUserDetails(usernameLength, usernameToByte);
        this.addUserDetails(receiverName.length(), convertStringToBytes(receiverName));
        String message = getAllMessages(msgTokens, 1);
        this.addUserDetails(message.length(), convertStringToBytes(message));
      }
      case INSULT_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.SEND_INSULT);
        this.addUserDetails(usernameLength, usernameToByte);
        this.addUserDetails(receiverName.length(), convertStringToBytes(receiverName));
      }
      case ALL_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.BROADCAST_MESSAGE);
        this.addUserDetails(usernameLength, usernameToByte);
        String message = getAllMessages(msgTokens, 1);
        this.addUserDetails(message.length(), convertStringToBytes(message));
      }
      case LOGIN_COMMAND -> {
        this.dataOutputStream.writeInt(MessageIdentifier.CONNECT_MESSAGE);
        this.addUserDetails(usernameLength, usernameToByte);
      }
      default -> {
        throw new IOException("Invalid command " + msgCommand + "!.");
      }
    }
  }

  /**
   * This private method is used to add user details, username length and username to byte in dataOutputStream
   *
   * @param usernameLength represented as int, username length
   * @param usernameToByte represented as Array of byte, username in bytes
   * @throws IOException exceptions produced by failed or interrupted I/O operations while writing in dataOutputStream
   */
  private void addUserDetails(int usernameLength, byte[] usernameToByte) throws IOException {
    this.dataOutputStream.writeInt(usernameLength);
    this.dataOutputStream.write(usernameToByte);
  }

  /**
   * This private method is used to get all messages in a single string
   *
   * @param msgTokens represented as Array of String, terminal command by user along with messages
   * @param startIndex represented as int, startIndex from were messages needed to be considered
   * @return represented as String, message
   */
  private String getAllMessages(String[] msgTokens, int startIndex) {
    List<String> strings = new ArrayList<>(Arrays.asList(msgTokens).subList(startIndex, msgTokens.length));
    return String.join(" ", strings);
  }

  /**
   * This private method is used to convert String to Array of byte
   *
   * @param str represented as String
   * @return represented as array of byte
   */
  private byte[] convertStringToBytes(String str) {
    return str.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * This public method is used to decode message by reading from dataInputStream and return decoded message as String
   *
   * @param dataInputStream represented as DataInputStream, read primitive data types from an underlying input stream in a machine-independent way
   * @return represented as String, decoded message
   * @throws IOException exceptions produced by failed or interrupted I/O operations while reading from dataInputStream
   */
  public String decodeMessage(DataInputStream dataInputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    int msgIdentifier = dataInputStream.readInt();
    switch (msgIdentifier) {
      case MessageIdentifier.CONNECT_RESPONSE -> {
        this.setConnected(dataInputStream.readBoolean());
        sb.append("Connection request is ").append(isConnected ? "successful." : "fail.").append(this.convertBytesToString(dataInputStream, dataInputStream.readInt()));
      }
      case MessageIdentifier.DISCONNECT_RESPONSE -> {
        boolean isConnect = dataInputStream.readBoolean();
        this.setConnected(!isConnect);
        sb.append("Disconnecting request is ").append(isConnected ? "fail." : "successful.").append(convertBytesToString(dataInputStream, dataInputStream.readInt()));
      }
      case MessageIdentifier.FAILED_MESSAGE ->
          sb.append(convertBytesToString(dataInputStream, dataInputStream.readInt()));
      case MessageIdentifier.QUERY_USER_RESPONSE -> {
        int userConnectedCount = dataInputStream.readInt();
        sb.append("There are ").append(userConnectedCount).append(" connected users: ");
        if(userConnectedCount > 0) {
          sb.append("[");
          String[] usersList = new String[userConnectedCount];
          for(int i = 0; i< userConnectedCount; i++) {
            usersList[i] = convertBytesToString(dataInputStream, dataInputStream.readInt());
          }
          sb.append(getAllMessages(usersList, 0)).append("]");
        }
      }
      case MessageIdentifier.BROADCAST_MESSAGE ->
          sb.append(convertBytesToString(dataInputStream, dataInputStream.readInt())).append(": ").append(convertBytesToString(dataInputStream, dataInputStream.readInt()));
      case MessageIdentifier.DIRECT_MESSAGE, MessageIdentifier.SEND_INSULT ->
          sb.append(convertBytesToString(dataInputStream, dataInputStream.readInt())).append("->").append(convertBytesToString(dataInputStream, dataInputStream.readInt())).append(": ").append(convertBytesToString(dataInputStream, dataInputStream.readInt()));
      default -> throw new IllegalStateException("Unexpected value: " + msgIdentifier);
    }
    return sb.toString();
  }

  /**
   * This private method is used to convert array of byte to String
   *
   * @param dataInputStream represented as DataInputStream, read primitive data types from an underlying input stream in a machine-independent way
   * @param length represented as int, length of array
   * @return represented as String
   * @throws IOException exceptions produced by failed or interrupted I/O operations while reading from dataInputStream
   */
  private String convertBytesToString(DataInputStream dataInputStream, int length)
      throws IOException {
    byte[] stringBytes = new byte[length];
    dataInputStream.readFully(stringBytes);
    return new String(stringBytes, StandardCharsets.UTF_8);
  }
}
