/**
 * InvalidArgException is a custom exception class extends runtime exception class.
 *
 * @author kabraambika19
 */
public class InvalidArgException extends RuntimeException {

  /**
   * Constructor of InvalidArgException
   * @param errorMessage represented as String, error Message
   */
  public InvalidArgException(String errorMessage) {
    super(errorMessage);
  }
}
