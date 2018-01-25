# http4j

http4j is an HTTP toolkit written in Java8 that allows for functional HTTP services -- in similar vein to [http4k](https://github.com/http4k/http4k) however geared for Java codebases.

In order to achieve simplicity in the abstractions & scope of the toolkit, certain restrictions to the HTTP spec have been chosen:
1. No Chunked Transfer Encoding -- every request & response must have a content-length
2. No Streaming -- the payloads are stored as byte arrays

## Functional
http4j follows Twitter's [Your Server as a Function](https://monkey.org/~marius/funsrv.pdf) whitepaper, and handlers are simple functional interfaces. To get started in http4j all you need to know the following two interfaces.
(they are in fact the same interface for the clients as well)

```java
@FunctionalInterface
public interface HttpHandler {

  HttpResponse handle(HttpRequest request);
}

@FunctionalInterface
public interface HttpFilter {

  HttpHandler handle(HttpHandler handler);
}
```


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
    int port = 8888;
    HttpServer server = new SunHttpServerCreator(port).create(router);
    server.start();
    
    HttpHandler client = new JdkClient();
    HttpResponse response = client.handle(HttpRequest.get((String.format("http://localhost:%s/ping", port))));
  }
}
```

There is even an in-memory transport to make testing simple

```java
public class InMemoryServerWithSimpleRouter {
  public static void main(String[] args) {
    Router router =
        Router.builder()
            .get("/ping", HttpHandlers.pong())
            .get("/echo/{message}", ServerWithSimpleRouter::echo)
            .build();
    InMemoryServer server = new InMemoryServerCreator().create(router);
    server.start();
    
    //the same functional interface, however goes directly to the handler
    //without the underlying HTTP transport layer
    HttpHandler client = server.getClient();
    HttpResponse response = client.handle(HttpRequest.get("http://localhost:%s/ping"));
  }
}
```

## TODO
1. Additional server creators
2. More tests
3. Github CICD
