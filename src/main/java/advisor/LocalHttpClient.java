package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static advisor.Main.*;

abstract class LocalHttpClient
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    static JsonObject makeHttpRequest(String apiPath)
    {
        if (apiPath == null) return new JsonObject();

        HttpRequest.Builder request = HttpRequest.newBuilder();
        HttpResponse<String> response = null;
        JsonObject result = new JsonObject();

        if ("AUTHORIZATION".equalsIgnoreCase(apiPath))
        {
            request.header("Content-Type", "application/x-www-form-urlencoded")
                   .header("Authorization", CLIENT_SECRET)
                   .uri(URI.create(spotifyAccountServer + "/api/token"))
                   .POST(HttpRequest.BodyPublishers.ofString(
                           "grant_type=authorization_code&redirect_uri=http://localhost:8080&code="
                           + codeForTokenRequest));
        }
        else
        {
            request.header("Authorization", "Bearer " + TOKEN.getOrDefault("access_token", ""))
                   .uri(URI.create(apiPath))
                   .GET();
        }

        try
        {
            response = HTTP_CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Error at 'LocalHttpClient': " + e.getMessage());
        }

        if (response != null && response.body() != null)
        {
            JsonElement parsedBody = JsonParser.parseString(response.body());

            if (parsedBody != null) result = parsedBody.getAsJsonObject();
        }

        return result;
    }

    static boolean getAccessToken()
    {
        if (codeForTokenRequest.isEmpty()) return false;

        System.out.println("Making HTTP request for access_token...");
        JsonObject responseJson = makeHttpRequest("AUTHORIZATION");

        isErrorObject(responseJson);

        if (responseJson.get("access_token") == null)
        {
            return false;
        }
        else
        {
            TOKEN.clear();
            for (Map.Entry<String, JsonElement> entry : responseJson.entrySet())
            {
                TOKEN.put(entry.getKey(), entry.getValue().getAsString());
            }
            return true;
        }
    }

    static boolean isErrorObject(JsonObject jsonResponse)
    {
        if (jsonResponse == null || jsonResponse.size() == 0)
        {
            System.out.println("---ERROR---");
            System.out.println("No data found in the JSON object.");
            return true;
        }

        var errorElement = jsonResponse.get("error");
        if (errorElement == null) return false;

        System.out.println("---ERROR---");

        if (errorElement.isJsonObject())
        {
            var errorObj = errorElement.getAsJsonObject();
            System.out.printf("HTTP %d%n%s%n%n",
                              errorObj.get("status").getAsInt(),
                              errorObj.get("message").getAsString()
            );
        }
        else
        {
            System.out.printf("%S%n%s%n%n",
                              errorElement.getAsString(),
                              jsonResponse.get("error_description").getAsString()
            );

        }
        return true;
    }
}