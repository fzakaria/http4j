package http4j.examples.simple;

import http4j.core.HttpHandlers;
import http4j.core.HttpServer;
import http4j.server.sun.SunHttpServerCreator;

public class PingPongServer {

  public static void main(String[] args) {
    HttpServer server = new SunHttpServerCreator(0).create(HttpHandlers.pong());
    server.start();
  }
}
