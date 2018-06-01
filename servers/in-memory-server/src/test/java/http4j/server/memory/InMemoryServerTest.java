package http4j.server.memory;

import http4j.core.HttpHandler;
import http4j.core.HttpHandlers;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import http4j.core.Router;
import http4j.server.memory.InMemoryServerCreator.InMemoryServer;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class InMemoryServerTest {

  @Test
  public void testPingPongClient() throws Exception {

    Router router = Router.builder().get("/ping", HttpHandlers.pong()).build();
    try (InMemoryServer server = new InMemoryServerCreator().create(router)) {
      server.start();

      HttpHandler client = server.getClient();
      HttpResponse response =
          client.handle(HttpRequest.get((String.format("http://localhost/ping"))));
      Assertions.assertThat(response.status()).isEqualTo(200);
    }
  }
}
