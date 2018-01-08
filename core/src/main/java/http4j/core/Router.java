package http4j.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import http4j.core.util.UriTemplate;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Very simple Router that uses linear lookup using {@link UriTemplate} */
public final class Router implements HttpHandler {

  // Matcher objects keyed by http methods.
  private final ListMultimap<HttpMethod, HandlerHolder> paths;

  // Handler called when no match was found and invalid method handler can't be invoked.
  private final HttpHandler fallbackHandler;

  // Handler called when this instance can not match the http method but can match another http method.
  // For example: For an exchange the POST method is not matched by this instance but at least one http method is
  // matched for the same exchange.
  // If this handler is null the fallbackHandler will be used.
  private final HttpHandler invalidMethodHandler;

  private Router(
      ListMultimap<HttpMethod, HandlerHolder> paths,
      HttpHandler fallbackHandler,
      HttpHandler invalidMethodHandler) {
    this.paths = paths;
    this.fallbackHandler = fallbackHandler;
    this.invalidMethodHandler = invalidMethodHandler;
  }

  @Override
  public HttpResponse handle(HttpRequest request) {

    final URI uri = request.uri();
    final String pathToMatch = uri.getPath();

    //This lookup is done in O(n) versus something like a Trie
    List<HandlerHolder> potentialPaths = paths.get(request.method());
    if (potentialPaths == null || potentialPaths.isEmpty()) {
      return handleNoMatch(request);
    }

    Optional<HandlerHolder> matchedPathOpt =
        potentialPaths.stream().filter(path -> path.template.matches(pathToMatch)).findFirst();

    if (!matchedPathOpt.isPresent()) {
      return handleNoMatch(request);
    }

    HandlerHolder matchedPath = matchedPathOpt.get();

    //hydrate the attribute map with the path parameters
    Map<String, String> pathParams = matchedPath.template.match(pathToMatch);
    for (Map.Entry<String, String> param : pathParams.entrySet()) {
      request = request.param(param.getKey(), param.getValue());
    }

    return matchedPath.handler.handle(request);
  }

  /**
   * Handles the case in with a match was not found for the http method but might exist for another
   * http method. For example: POST not matched for a path but at least one match exists for same
   * path.
   */
  private HttpResponse handleNoMatch(HttpRequest request) {
    boolean wrongMethodExists =
        paths.values().stream().anyMatch(path -> path.template.matches(request.uri().getPath()));
    if (wrongMethodExists) {
      return invalidMethodHandler.handle(request);
    }
    return fallbackHandler.handle(request);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ListMultimap<HttpMethod, HandlerHolder> paths = ArrayListMultimap.create();
    private HttpHandler fallbackHandler = HttpHandlers.notFound();
    private HttpHandler invalidMethodHandler = HttpHandlers.invalidMethod();

    public Builder() {}

    public Builder fallbackHandler(HttpHandler handler) {
      this.fallbackHandler = handler;
      return this;
    }

    public Builder invalidMethodHandler(HttpHandler handler) {
      this.invalidMethodHandler = handler;
      return this;
    }

    public Builder handler(HttpMethod method, String template, HttpHandler handler) {
      this.paths.put(method, new HandlerHolder(UriTemplate.parse(template), handler));
      return this;
    }

    public Builder get(String template, HttpHandler handler) {
      return handler(HttpMethod.GET, template, handler);
    }

    public Builder post(String template, HttpHandler handler) {
      return handler(HttpMethod.POST, template, handler);
    }

    public Builder put(String template, HttpHandler handler) {
      return handler(HttpMethod.PUT, template, handler);
    }

    public Builder delete(String template, HttpHandler handler) {
      return handler(HttpMethod.DELETE, template, handler);
    }

    public Builder head(String template, HttpHandler handler) {
      return handler(HttpMethod.HEAD, template, handler);
    }

    public Router build() {
      return new Router(paths, fallbackHandler, invalidMethodHandler);
    }
  }

  private static final class HandlerHolder {
    private final UriTemplate template;
    private final HttpHandler handler;

    private HandlerHolder(UriTemplate template, HttpHandler handler) {
      this.template = template;
      this.handler = handler;
    }
  }
}
