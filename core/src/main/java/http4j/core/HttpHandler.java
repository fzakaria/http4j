package http4j.core;

/** Main handler for all HTTP requests */
@FunctionalInterface
public interface HttpHandler {

  HttpResponse handle(HttpRequest request);
}
