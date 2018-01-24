package http4j.server.memory;

import http4j.client.memory.InMemoryClient;
import http4j.core.HttpHandler;
import http4j.core.HttpServer;
import http4j.core.HttpServerCreator;
import java.util.Objects;

/**
 * Create an {@link InMemoryServer} that avoids Http transport. This can be very useful for unit
 * tests where you don't need to start up real server.
 */
public class InMemoryServerCreator implements HttpServerCreator {

  public static class InMemoryServer implements HttpServer {

    private final HttpHandler handler;

    public InMemoryServer(HttpHandler handler) {
      this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public int getPort() {
      throw new UnsupportedOperationException("InMemoryServer does not listen on a port.");
    }

    @Override
    public void start() {
      //do nothing
    }

    @Override
    public void close() throws Exception {
      //do nothing
    }

    /** Create an in-memory http client */
    public HttpHandler getClient() {
      return new InMemoryClient(handler);
    }
  }

  @Override
  public HttpServer create(HttpHandler handler) {
    return new InMemoryServer(handler);
  }
}
