package http4j.server.sun;

import com.google.common.base.Splitter;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import http4j.core.HttpHandler;
import http4j.core.HttpMethod;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import http4j.core.HttpServer;
import http4j.core.HttpServerCreator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super simple server using the JDK {@link HttpServer}. It tries to follow some functional
 * paradigms and have the request/response objects copy-on-write / immutable
 */
public final class SunHttpServerCreator implements HttpServerCreator {

  private static final Logger LOG = LoggerFactory.getLogger(SunHttpServer.class);

  public static final class SunHttpServer implements HttpServer {

    private final com.sun.net.httpserver.HttpServer server;

    private SunHttpServer(com.sun.net.httpserver.HttpServer server) {
      this.server = Objects.requireNonNull(server);
    }

    public int getPort() {
      return server.getAddress().getPort();
    }

    @Override
    public void start() {
      LOG.info("Starting server on port {}", getPort());
      this.server.start();
    }

    @Override
    public void close() throws Exception {
      LOG.info("Stopping server on port {}", getPort());
      this.server.stop(0);
    }
  }

  private final com.sun.net.httpserver.HttpServer server;

  private static final Executor DEFAULT_EXECUTOR =
      Executors.newCachedThreadPool(
          new ThreadFactoryBuilder()
              .setNameFormat("jdk-http-server-%d")
              .setUncaughtExceptionHandler(
                  (t, e) -> LOG.error("Uncaught unexception for {}", t.getName(), e))
              .build());

  public SunHttpServerCreator(int port) {
    this(port, DEFAULT_EXECUTOR);
  }

  public SunHttpServerCreator(int port, Executor executor) {
    try {
      this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
      server.setExecutor(executor);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public HttpServer create(HttpHandler handler) {
    server.createContext(
        "/",
        httpExchange -> {
          if (isChunkedTransferEncoding(httpExchange)) {
            LOG.trace("http4j only accepts http requests with content length.");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_LENGTH_REQUIRED, -1);
            return;
          }

          try {
            HttpResponse response = handler.handle(convert(httpExchange));
            //Take the response and use it
            response
                .headers()
                .entries()
                .forEach(
                    entry -> {
                      httpExchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
                    });
            httpExchange.sendResponseHeaders(response.status(), response.length());
            ByteStreams.copy(response.body(), httpExchange.getResponseBody());
          } catch (Throwable t) {
            LOG.error("Uncaught error thrown.", t);
            httpExchange.sendResponseHeaders(500, -1);
          }
          httpExchange.close();
        });
    return new SunHttpServer(server);
  }

  /** Create a {@link HttpRequest} from an {@link HttpExchange} */
  private static HttpRequest convert(HttpExchange exchange) {
    //Collect the headers
    Multimap<String, String> headers =
        exchange
            .getRequestHeaders()
            .entrySet()
            .stream()
            .collect(
                LinkedListMultimap::create,
                (mm, entry) -> mm.putAll(entry.getKey(), entry.getValue()),
                Multimap::putAll);

    //hydrate the attribute map with the query parameters
    final String query = exchange.getRequestURI().getQuery();
    Map<String, String> params = new HashMap<>();
    if (query != null) {
      //TODO: replace with a more full fledge decoder
      params = Splitter.on('&').trimResults().withKeyValueSeparator("=").split(query);
    }

    HttpMethod method = HttpMethod.method(exchange.getRequestMethod());
    int length =
        Optional.ofNullable(exchange.getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH))
            .map(Ints::tryParse)
            .orElse(0);

    return new HttpRequest(
        method,
        exchange.getRequestBody(),
        length,
        headers,
        exchange.getRequestURI(),
        exchange.getRemoteAddress(),
        params);
  }

  private static boolean isChunkedTransferEncoding(HttpExchange exchange) {
    return exchange.getRequestHeaders().containsKey(HttpHeaders.TRANSFER_ENCODING)
        && exchange.getRequestHeaders().get(HttpHeaders.TRANSFER_ENCODING).contains("chunked");
  }
}
