package http4j.core;

@FunctionalInterface
public interface HttpFilter {

  HttpHandler handle(HttpHandler handler);
}
