package http4j.client.jdk;

import http4j.core.HttpHandler;
import http4j.core.HttpHandlers;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import http4j.core.HttpServer;
import http4j.core.PortSelector;
import http4j.core.Router;
import http4j.server.sun.SunHttpServerCreator;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JdkClientTest {

  @Test
  public void testPingPongClient() throws Exception {

    int port = PortSelector.getAvailablePort();

    Router router = Router.builder().get("/ping", HttpHandlers.pong()).build();
    try (HttpServer server = new SunHttpServerCreator(port).create(router)) {
      server.start();

      HttpHandler client = new JdkClient();
      HttpResponse response =
          client.handle(HttpRequest.get((String.format("http://localhost:%s/ping", port))));
      Assertions.assertThat(response.status()).isEqualTo(200);
    }
  }
}
