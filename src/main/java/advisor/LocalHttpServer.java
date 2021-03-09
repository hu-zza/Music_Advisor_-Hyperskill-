package advisor;

import static advisor.Main.codeForTokenRequest;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

abstract class LocalHttpServer {

  private static final String[] RESPONSE_TEXT = {
      "Got the code. Return back to your program.",
      "Not found authorization code. Try again."
  };
  private static final String HTML_WRAPPER =
      "<html><head><title>Authentication | Music Advisor</title></head><body><center>%s</center></body></html>";
  private static boolean serverStatus = false;
  private static HttpServer serverForCallback;

  static void start() {
    if (!serverStatus) {
      try {
        serverForCallback = HttpServer.create();
        serverForCallback.bind(new InetSocketAddress(8080), 0);
        serverStatus = true;
      } catch (IOException e) {
        serverStatus = false;
        System.out.println("Error at 'LocalServer': " + e.getMessage());
      }

      if (serverForCallback != null) {
        serverForCallback.createContext(
            "/",
            exchange -> {
              String query = exchange.getRequestURI().getQuery();
              if (query != null) {
                String[] queryArr = query.split("[?=&]");
                for (int x = 0; x < queryArr.length; x++) {
                  if ("CODE".equalsIgnoreCase(queryArr[x]) && x + 1 < queryArr.length) {
                    codeForTokenRequest = queryArr[x + 1];
                  }
                }
              }
              String htmlContent = String.format(
                  HTML_WRAPPER, codeForTokenRequest.isEmpty() ? RESPONSE_TEXT[1] : RESPONSE_TEXT[0]
              );

              exchange.sendResponseHeaders(200, htmlContent.length());
              exchange.getResponseBody().write(htmlContent.getBytes());
              exchange.getResponseBody().close();
            }
        );
        serverForCallback.start();
        serverStatus = true;
      }
    }
  }

  static void stop() {
    if (serverStatus) {
      serverForCallback.stop(1);
      serverStatus = false;
    }
  }
}