package http4j.core;

public interface HttpServer extends AutoCloseable {

  /**
   * Get the port the http server is listening on.
   *
   * @return the port that is being listened to
   */
  int getPort();

  /** start the http server */
  void start();
}
