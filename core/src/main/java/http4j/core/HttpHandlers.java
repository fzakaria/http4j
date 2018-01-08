package http4j.core;

import java.net.HttpURLConnection;

/** Common handlers */
public final class HttpHandlers {

  private HttpHandlers() {}

  /** Ping handler. Returns status 200 and a pong message. */
  public static HttpHandler pong() {
    return request -> HttpResponse.status(HttpURLConnection.HTTP_OK).body("pong");
  }

  /** Simpler handler for returning a response code and nothing else. */
  public static HttpHandler responseCode(int code) {
    return (request) -> HttpResponse.status(code);
  }

  /** Simple handler returns 404 for not found */
  public static HttpHandler notFound() {
    return responseCode(HttpURLConnection.HTTP_NOT_FOUND);
  }

  /** Simple handler returns 405 for invalid method */
  public static HttpHandler invalidMethod() {
    return responseCode(HttpURLConnection.HTTP_BAD_METHOD);
  }
}
