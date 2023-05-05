/**
 * MessageIdentifier class is used to represent message identifier of protocol as constants
 *
 * @author kabraambika19
 */
public class MessageIdentifier {

  /**
   * Constant for CONNECT_MESSAGE command = 19
   */
  public static final int CONNECT_MESSAGE = 19;

  /**
   * Constant for CONNECT_RESPONSE command = 20
   */
  public static final int CONNECT_RESPONSE = 20;

  /**
   * Constant for DISCONNECT_MESSAGE command = 21
   */
  public static final int DISCONNECT_MESSAGE = 21;

  /**
   * Constant for QUERY_CONNECTED_USERS command = 22
   */
  public static final int QUERY_CONNECTED_USERS = 22;

  /**
   * Constant for QUERY_USER_RESPONSE command = 23
   */
  public static final int QUERY_USER_RESPONSE = 23;

  /**
   * Constant for BROADCAST_MESSAGE command = 24
   */
  public static final int BROADCAST_MESSAGE = 24;

  /**
   * Constant for DIRECT_MESSAGE command = 25
   */
  public static final int DIRECT_MESSAGE = 25;

  /**
   * Constant for FAILED_MESSAGE command = 26
   */
  public static final int FAILED_MESSAGE = 26;

  /**
   * Constant for SEND_INSULT command = 27
   */
  public static final int SEND_INSULT = 27;

  /**
   * Constant for DISCONNECT_RESPONSE command = 28
   */
  public static final int DISCONNECT_RESPONSE = 28;

  /**
   * Private constructor of MessageIdentifier
   */
  private MessageIdentifier() {
  }
}
