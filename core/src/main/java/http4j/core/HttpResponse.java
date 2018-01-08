package http4j.core;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.annotation.concurrent.Immutable;

/**
 * A "copy-on-write" type of HttpResponse. Any modification to the request should go through its
 * copy builder. The body of the response is stored <b>in-memory</b> as a {@link ByteBuffer} -- so
 * watch out if you are sending huge payloads, since there is no ability to stream.
 */
@Immutable
public final class HttpResponse extends HttpMessage {

  private final ByteBuffer buffer;
  private final int status;

  private HttpResponse(int status, ByteBuffer buffer, Multimap<String, String> headers) {
    super(headers);
    this.buffer = buffer;
    this.status = status;
  }

  /**
   * Create a new {@link HttpResponse} provided the http status
   *
   * @param status the http status code to initialize the response with
   */
  public static HttpResponse status(int status) {
    return new HttpResponse(status, ByteBuffer.allocate(0), LinkedListMultimap.create());
  }

  /** the http status code to return */
  public int status() {
    return status;
  }

  /** the current size of the body. this will turn into the value placed for content-length. */
  @Override
  public int length() {
    return buffer.array().length;
  }

  /**
   * In order to wrap the body for downstream handling in filters/middleware, its returned here as
   * an inputstream.
   *
   * @return
   */
  public InputStream body() {
    return new ByteArrayInputStream(buffer.array());
  }

  /**
   * A way to fluently perform copy-on-write This method will first create a copy at which point you
   * can modify the values before finalizing.
   */
  public CopyBuilder copy() {
    return new CopyBuilder(this);
  }

  public HttpResponse body(String body) {
    return body(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
  }

  public HttpResponse body(ByteBuffer body) {
    return copy().body(body).build();
  }

  public HttpResponse header(String key, String value) {
    return copy().header(key, value).build();
  }

  public static class CopyBuilder {

    private ByteBuffer buffer;
    private int status;

    private final Multimap<String, String> headers;

    public CopyBuilder(HttpResponse response) {
      this.buffer = response.buffer;
      this.status = response.status;
      this.headers = LinkedListMultimap.create(response.headers());
    }

    public CopyBuilder status(int status) {
      this.status = status;
      return this;
    }

    public CopyBuilder body(ByteBuffer body) {
      this.buffer = body;
      return this;
    }

    public CopyBuilder body(String body) {
      return body(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
    }

    public CopyBuilder header(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    public HttpResponse build() {
      return new HttpResponse(status, buffer, headers);
    }
  }
}
