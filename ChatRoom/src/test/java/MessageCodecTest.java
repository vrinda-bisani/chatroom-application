import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageCodecTest {
  private MessageCodec codecTest;
  private DataOutputStream dataOutputStream;
  @BeforeEach
  void setUp() throws IOException {
    dataOutputStream = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    codecTest = new MessageCodec(dataOutputStream);
  }

  @Test
  void isConnected() {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    assertTrue(codecTest.isConnected());
  }

  @Test
  void setConnected() {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    assertTrue(codecTest.isConnected());
  }

  @Test
  void setUsername() {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    assertTrue(codecTest.isConnected());
  }

  @Test
  void encodeMessageBroadCast() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("@all hello");
  }

  @Test
  void encodeMessageDirect() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("@amy hello");
  }

  @Test
  void encodeMessageInsult() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("!amy");
  }

  @Test
  void encodeMessageExp() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    assertThrows(IOException.class, ()->{
      codecTest.encodeMessage("sdfsdf");
    });
  }

  @Test
  void encodeMessageLogoff() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("logoff");
  }

  @Test
  void encodeMessageLogin() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("login");
  }

  @Test
  void encodeMessageWho() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    codecTest.encodeMessage("who");
  }

  @Test
  void encodeMessage_ERROR() {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");
    assertThrows(IOException.class, ()-> {
      codecTest.encodeMessage("$amy");
    });

  }

  @Test
  void decodeMessage() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "\u0000\u0000\u0000\u0018\u0000\u0000\u0000\u0003amy\u0000\u0000\u0000\u0005hello";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertEquals("amy: hello",codecTest.decodeMessage(dataIn));
  }

  @Test
  void decodeMessageQuery() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "\u0000\u0000\u0000\u0017\u0000\u0000\u0000\u0001\u0000\u0000\u0000\u0003bob";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertEquals("There are 1 connected users: [bob]",codecTest.decodeMessage(dataIn));
  }

  @Test
  void decodeMessageLogoff() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "\u0000\u0000\u0000\u001C\u0001\u0000\u0000\u0000\u001BYou are no longer connected";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertEquals("Disconnecting request is successful.You are no longer connected",codecTest.decodeMessage(dataIn));
  }

  @Test
  void decodeMessageDirect() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "\u0000\u0000\u0000\u0019\u0000\u0000\u0000\u0003amy\u0000\u0000\u0000\u0003bob\u0000\u0000\u0000\u0005hello";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertEquals("amy->bob: hello",codecTest.decodeMessage(dataIn));
  }

  @Test
  void decodeMessageLogin() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "\u0000\u0000\u0000\u0014\u0001\u0000\u0000\u0000#There are 1 other connected clients";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertEquals("Connection request is successful.There are 1 other connected clients",codecTest.decodeMessage(dataIn));
  }

  @Test
  void decodeMessage_error() throws IOException {
    codecTest.setConnected(Boolean.TRUE);
    codecTest.setUsername("amy");

    String str = "@all";

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("src/test/outputFile.txt"));
    byte[] b = str.getBytes();
    dos.write(b);
    dos.flush();
    dos.close();
    DataInputStream dataIn = new DataInputStream(new FileInputStream("src/test/outputFile.txt"));
    assertThrows(IllegalStateException.class, ()-> {
      codecTest.decodeMessage(dataIn);
    });
  }
}