package http4j.client.memory;

import http4j.core.HttpHandler;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import java.util.Objects;

public class InMemoryClient implements HttpHandler {

  private final HttpHandler server;

  /** Constructor that passes all requests to the underlying server's {@link HttpHandler} */
  public InMemoryClient(HttpHandler server) {
    this.server = Objects.requireNonNull(server);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    return server.handle(request);
  }
}
