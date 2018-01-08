# http4j

http4j is an HTTP toolkit written in Java8 that allows for functional HTTP services -- in similar vein to [http4k](https://github.com/http4k/http4k) however geared for Java codebases.

In order to achieve simplicity in the abstractions & scope of the toolkit, certain restrictions to the HTTP spec have been chosen:
1. No Chunked Transfer Encoding -- every request & response must have a content-length
2. No Streaming -- the payloads are stored as byte arrays

## Example

```java
public class ServerWithSimpleRouter {

  private static HttpResponse echo(HttpRequest request) {
    String message = request.params().get("message");
    return HttpResponse.status(200).body(message);
  }

  public static void main(String[] args) {
    Router router =
        Router.builder()
            .get("/ping", HttpHandlers.pong())
            .get("/echo/{message}", ServerWithSimpleRouter::echo)
            .build();
    HttpServer server = new SunHttpServerCreator(0).create(router);
    server.start();
  }
}
```

## TODO
1. Client interface will be the same as HttpHandler but should have several implementations
2. In-Memory server & client
3. Additional server creators
4. More tests
5. Github CICD
