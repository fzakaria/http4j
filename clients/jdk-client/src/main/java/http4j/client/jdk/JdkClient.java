package http4j.client.jdk;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import http4j.core.HttpHandler;
import http4j.core.HttpRequest;
import http4j.core.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Create a JDK client that uses {@link java.net.HttpURLConnection} A helpful library {@link
 * com.github.kevinsawicki.http.HttpRequest} to help with the translation.
 */
public class JdkClient implements HttpHandler {

  @Override
  public HttpResponse handle(HttpRequest request) {
    try {
      com.github.kevinsawicki.http.HttpRequest jdkRequest =
          new com.github.kevinsawicki.http.HttpRequest(
              request.uri().toURL(), request.method().name());
      for (Map.Entry<String, String> header : request.headers().entries()) {
        jdkRequest.header(header.getKey(), header.getValue());
      }

      if (request.length() > 0) {
        jdkRequest.send(request.body());
      }

      //copy the body
      final ByteBuffer body;
      int length = jdkRequest.contentLength();

      if (length <= 0) {
        body = ByteBuffer.allocate(0);
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        ByteStreams.copy(jdkRequest.stream(), baos);
        body = ByteBuffer.wrap(baos.toByteArray());
      }

      Multimap<String, String> headers = ArrayListMultimap.create();
      for (Map.Entry<String, List<String>> entry : jdkRequest.headers().entrySet()) {
        headers.putAll(entry.getKey(), entry.getValue());
      }
      return new HttpResponse(jdkRequest.code(), body, headers);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
