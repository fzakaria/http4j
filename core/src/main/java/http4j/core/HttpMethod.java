package http4j.core;

public enum HttpMethod {
  GET,
  DELETE,
  POST,
  PUT,
  UPDATE,
  HEAD;

  /**
   * Convert the string ignoring case to a {@link HttpMethod}
   *
   * @throws IllegalArgumentException if one cannot be found
   */
  public static HttpMethod method(String method) {
    for (HttpMethod m : HttpMethod.values()) {
      if (m.name().equalsIgnoreCase(method)) {
        return m;
      }
    }
    throw new IllegalArgumentException("No HttpMethod can be found for " + method);
  }
}
