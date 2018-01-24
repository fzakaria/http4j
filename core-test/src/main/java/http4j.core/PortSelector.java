package http4j.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortSelector {

  private static final Logger LOG = LoggerFactory.getLogger(PortSelector.class);

  private static final int MIN_PORT = 10000;
  private static final int MAX_PORT = 20000;
  private static int nextPort = MIN_PORT;

  /** Private constructor. */
  private PortSelector() {}

  /**
   * Provides an available port on the local server in a low port range (between 10,000 and 20,000)
   * to avoid clashing with ephemeral port allocation.
   *
   * @return An available port number
   */
  public static int getAvailablePort() {
    // try to find an available port on INADDR_ANY.
    final int MAX_RETRY_COUNT = 100;
    int retryCount = 0;
    while (retryCount++ < MAX_RETRY_COUNT) {
      // find an available port
      InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
      int testPort;
      synchronized (PortSelector.class) {
        testPort = nextPort++;
        if (nextPort > MAX_PORT) {
          nextPort = MIN_PORT;
        }
      }
      try (ServerSocket socket = new ServerSocket()) {
        socket.setReuseAddress(false);
        socket.bind(new InetSocketAddress(loopbackAddress, testPort));
        LOG.info("getAvailablePort = {}", testPort);
        return testPort;
      } catch (IOException ex) {
        LOG.info("port {} is in use. Retrying.", testPort);
      }
    }
    throw new RuntimeException("Cannot find an available port");
  }
}
