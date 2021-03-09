package advisor;

import static advisor.Main.CLIENT_ID;
import static advisor.Main.authorized;
import static advisor.Main.codeForTokenRequest;
import static advisor.Main.spotifyAccountServer;
import static advisor.Main.spotifyApiServer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Controller {

  static void authorization() {
    System.out.println("Use this link to request the access code:");
    System.out.println(spotifyAccountServer + "/authorize?client_id=" + CLIENT_ID
        + "&redirect_uri=http://localhost:8080&response_type=code");
    System.out.println("Waiting for code...");

    waitingForCode(120);

    System.out
        .println(codeForTokenRequest.isEmpty() ? "Code has not received." : "Code has received.");

    if (authorized = LocalHttpClient.getAccessToken()) {
      // Playlist fetching needs initialized category list:
      fetchFromSpotify("/v1/browse/categories", "__CATEGORIES__");
      System.out.println("---SUCCESS---");
    } else {
      System.out.println("---FAIL---");
    }
  }

  private static void waitingForCode(int waitingTimeInSeconds) {
    for (int x = 0; x < waitingTimeInSeconds * 2 && codeForTokenRequest.isEmpty(); x++) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        System.out.println("Error at 'Controller': " + e.getMessage());
      }
    }
  }

  static void list(SpotifyList listName) {
    list(listName, "");
  }

  static void list(SpotifyList listName, String playlistName) {
    switch (listName) {
      case NEW:
        fetchFromSpotify("/v1/browse/new-releases", "__NEW_RELEASES__");
        setViewerList("__NEW_RELEASES__");
        break;

      case FEATURED:
        fetchFromSpotify("/v1/browse/featured-playlists", "__FEATURED__");
        setViewerList("__FEATURED__");
        break;

      case CATEGORIES:
        fetchFromSpotify("/v1/browse/categories", "__CATEGORIES__");
        setViewerList("__CATEGORIES__");
        break;

      case PLAYLISTS:
        String id = Model.getCategoryId(playlistName);
        if (id.isEmpty()) {
          System.out.println("---ERROR---");
          System.out.println("Can not find the specified playlist.");
          return;
        }
        fetchFromSpotify("/v1/browse/categories/" + id + "/playlists", id);
        setViewerList(id);
        break;
    }

    Viewer.view();
  }

  private static void fetchFromSpotify(String apiPath, String playlistId) {
    // This is the place of 'refresh outdated lists only' logic... :-)
      if (Model.containsPlaylist(playlistId)) {
          return;
      }

    String containerType;
    switch (playlistId) {
      case "__NEW_RELEASES__":
        containerType = "albums";
        break;

      case "__CATEGORIES__":
        containerType = "categories";
        break;

      case "__FEATURED__":
      default:
        containerType = "playlists";
        break;
    }

    int offset = 0;
    int limit = 50;
    JsonObject responseJson;
    JsonObject container;
    JsonArray items;

    Model.initializePlaylist(playlistId);

    do {
      responseJson = LocalHttpClient.makeHttpRequest(
          spotifyApiServer + apiPath + "?limit=" + limit + "&offset=" + offset
      );

        if (LocalHttpClient.isErrorObject(responseJson)) {
            return;
        }

      container = responseJson.get(containerType).getAsJsonObject();
      items = container.get("items").getAsJsonArray();

      if ("__CATEGORIES__".equals(playlistId)) {
        saveToCategoryCatalog(items);
      } else {
        saveToPlaylists(items, playlistId);
      }

      offset += limit;

    } while (!container.get("next").isJsonNull());
  }

  private static void saveToCategoryCatalog(JsonArray items) {
    Model.clearAllCategory();
    JsonObject item;

    for (JsonElement e : items) {
      item = e.getAsJsonObject();
      Model.addCategory(
          item.get("id").getAsString(),
          item.get("name").getAsString()
      );
    }
  }

  private static void saveToPlaylists(JsonArray items, String playlistId) {
    JsonObject item;
    JsonArray artists;
    String[] artistList = new String[0];

    for (JsonElement e : items) {
      item = e.getAsJsonObject();

      if ("__NEW_RELEASES__".equals(playlistId)) {
        artists = item.get("artists").getAsJsonArray();
        artistList = new String[artists.size()];

        for (int x = 0; x < artistList.length; x++) {
          artistList[x] = artists.get(x).getAsJsonObject().get("name").getAsString();
        }
      }

      Model.addItemToPlaylist(
          playlistId,
          new MusicEntity(
              item.get("name").getAsString(),
              artistList,
              item.get("external_urls").getAsJsonObject().get("spotify").getAsString()
          )
      );
    }
  }

  private static void setViewerList(String playlistId) {
    if ("__CATEGORIES__".equals(playlistId)) {
      Viewer.setList(Model.getCategoryList());
      return;
    }

    Viewer.setList(Model.getStringPlaylist(playlistId));
  }
}