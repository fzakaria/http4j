package http4j.examples.simple;

import http4j.core.HttpHandlers;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import http4j.core.HttpServer;
import http4j.core.Router;
import http4j.server.sun.SunHttpServerCreator;

public class ServerWithSimpleRouter {

  private static HttpResponse echo(HttpRequest request) {
    String message = request.params().get("message");
    return HttpResponse.status(200).body(message);
  }

  public static void main(String[] args) {
    Router router =
        Router.builder()
            .get("/ping", HttpHandlers.pong())
            .get("/echo/{message}", ServerWithSimpleRouter::echo)
            .build();
    HttpServer server = new SunHttpServerCreator(0).create(router);
    server.start();
  }
}
