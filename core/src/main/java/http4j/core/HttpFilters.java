package http4j.core;

import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A collection of common middleware that might be useful for an HTTP server. */
public final class HttpFilters {

  private static final DateTimeFormatter COMMON_LOG_DATE_FORMAT =
      DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z").withZone(ZoneId.systemDefault());

  private static final Logger LOG = LoggerFactory.getLogger(HttpFilters.class);

  /** Set this log name in the log4j2.xml if you want to send access logs to a specific appender. */
  private static final Logger ACCESS_LOG = LoggerFactory.getLogger("http4j.access_log");

  private HttpFilters() {}

  /** Wrap a collection of common middleware around the provided handler. */
  public static HttpHandler common(HttpHandler handler) {
    return accessLog(gzip(handler));
  }

  /**
   * This is a basic implementation of the CommonLogFormat
   * https://en.wikipedia.org/wiki/Common_Log_Format
   */
  public static HttpHandler accessLog(HttpHandler handler) {
    return (request) -> {
      //save the current time request was started
      Instant now = Instant.now();

      HttpResponse response = handler.handle(request);
      ACCESS_LOG.info(
          "{} - - [{}] \"{} {}\" {} {} {}",
          request.remote(),
          COMMON_LOG_DATE_FORMAT.format(now),
          request.method().name(),
          request.uri(),
          request.protocol(),
          response.status(),
          response.length());

      return response;
    };
  }

  public static HttpHandler gzip(HttpHandler handler) {
    return (request) -> {
      HttpResponse response = handler.handle(request);
      try {
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        GZIPOutputStream gzipOs = new GZIPOutputStream(baOs);
        ByteStreams.copy(response.body(), gzipOs);
        gzipOs.close(); //important to finish the trailing bytes

        return response
            .header(HttpHeaders.CONTENT_ENCODING, "gzip")
            .body(ByteBuffer.wrap(baOs.toByteArray()));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }
}
