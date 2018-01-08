package http4j.core;

public interface HttpServerCreator {

  /** Create a {@link HttpServer} given the provided {@link HttpHandler} */
  HttpServer create(HttpHandler handler);
}
