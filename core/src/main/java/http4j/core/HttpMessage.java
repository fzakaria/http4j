package http4j.core;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import http4j.core.util.CaseInsensitiveMultimap;

public abstract class HttpMessage {

  private final Multimap<String, String> headers;

  protected HttpMessage(Multimap<String, String> headers) {
    this.headers = CaseInsensitiveMultimap.create(headers);
  }

  /** Return unmodifiable view of the headers */
  public Multimap<String, String> headers() {
    return Multimaps.unmodifiableMultimap(headers);
  }

  /** Http protocol version */
  public String protocol() {
    return "HTTP/1.1";
  }

  /**
   * The content-length of the http message
   *
   * @return the length in bytes
   */
  public abstract int length();
}
