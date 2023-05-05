import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatRoomClientTest {
  private ChatRoomClient clientTest;

  @BeforeEach
  void setUp() {
    clientTest = new ChatRoomClient(9999, "localhost");
  }

  @Test
  void main_noArgs() {
    assertThrows(InvalidArgException.class, () -> {
      ChatRoomClient.main(new String[]{});
    });
  }

  @Test
  void main_NoPort() {
    assertThrows(InvalidArgException.class, () -> {
      ChatRoomClient.main(new String[]{"sdf","localhost"});
    });
  }

  @Test
  void mainTest_who() {
    Thread s = new Thread(() -> {
      try {
        startChatServer3();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    s.start();

    Thread client1 = new Thread(() -> {
      String data = "amy\n?\nwho\n@all hello\n@amy hey\nlogoff\n";
      InputStream stdin = System.in;
      try {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        ChatRoomClient.main(new String[]{"8080", "localhost"});
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
          System.out.println(scanner.nextLine());
        }
      } finally {
        System.setIn(stdin);
      }
    });
    client1.start();
  }

  @Test
  void mainTest_insult() {
    Thread s = new Thread(() -> {
      try {
        startChatServer2();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    s.start();

    Thread client1 = new Thread(() -> {
      String data = "bob\n!bob\nlogoff\n";
      InputStream stdin = System.in;
      try {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        ChatRoomClient.main(new String[]{"8085", "localhost"});
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
          System.out.println(scanner.nextLine());
        }
      } finally {
        System.setIn(stdin);
      }
    });
    client1.start();
  }

  @Test
  void mainTest_wrong() {
    Thread s = new Thread(() -> {
      try {
        startChatServer1();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    s.start();

    Thread client1 = new Thread(() -> {
      String data = "man\n@bob yo\nlogoff\n";
      InputStream stdin = System.in;
      try {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        ChatRoomClient.main(new String[]{"12345", "localhost"});
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
          System.out.println(scanner.nextLine());
        }
      } finally {
        System.setIn(stdin);
      }
    });
    client1.start();
  }

  private void startChatServer1() throws IOException {
    ChatRoomServer.main(new String[] {"12345"});
  }
  private void startChatServer3() throws IOException {
    ChatRoomServer.main(new String[] {"8080"});
  }

  private void startChatServer2() throws IOException {
    ChatRoomServer.main(new String[] {"8085"});
  }
}