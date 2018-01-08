package http4j.core;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import http4j.core.util.CaseInsensitiveMap;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * A "copy-on-write" type of HttpRequest. Any modification to the request should go through its copy
 * builder.
 */
@Immutable
public class HttpRequest extends HttpMessage {

  private final InputStream body;
  private final int length;
  private final HttpMethod method;
  private final URI uri;
  private final InetSocketAddress remote;
  private final CaseInsensitiveMap<String> params;

  public HttpRequest(
      HttpMethod method,
      InputStream body,
      int length,
      Multimap<String, String> headers,
      URI uri,
      InetSocketAddress remote,
      Map<String, String> params) {
    super(headers);
    this.method = method;
    this.body = body;
    this.length = length;
    this.uri = uri;
    this.remote = remote;
    this.params = CaseInsensitiveMap.create(params);
  }

  /** Get the request method */
  public HttpMethod method() {
    return method;
  }

  /** The body of the request. Can only be read once. */
  public InputStream body() {
    return body;
  }

  @Override
  public int length() {
    return length;
  }

  /** Get the request URI */
  public URI uri() {
    return uri;
  }

  /** Returns the address of the remote entity invoking this request */
  public InetSocketAddress remote() {
    return remote;
  }

  /**
   * Returns a non-modifiable map of the params for this request. This includes query params and
   * path params.
   */
  public Map<String, String> params() {
    return Collections.unmodifiableMap(params);
  }

  /**
   * A way to fluently perform copy-on-write This method will first create a copy at which point you
   * can modify the values before finalizing.
   */
  public CopyBuilder copy() {
    return new CopyBuilder(this);
  }

  public HttpRequest param(String key, String value) {
    return copy().param(key, value).build();
  }

  public static class CopyBuilder {

    private InputStream body;
    private final int length;
    private final Multimap<String, String> headers;
    private final URI uri;
    private final HttpMethod method;
    private final InetSocketAddress remote;
    private final Map<String, String> params;

    public CopyBuilder(HttpRequest request) {
      this.body = request.body();
      this.length = request.length();
      this.method = request.method();
      this.headers = LinkedListMultimap.create(request.headers());
      this.uri = request.uri();
      this.remote = request.remote();
      this.params = new HashMap<>(request.params());
    }

    public CopyBuilder body(InputStream body) {
      this.body = body;
      return this;
    }

    public CopyBuilder param(String key, String value) {
      this.params.put(key, value);
      return this;
    }

    public HttpRequest build() {
      return new HttpRequest(method, body, length, headers, uri, remote, params);
    }
  }
}
