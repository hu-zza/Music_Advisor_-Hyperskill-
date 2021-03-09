package advisor;

import java.util.Arrays;

class MusicEntity {

  private final String name;
  private final String[] artists;
  private final String external_url_spotify;

  MusicEntity(String name, String[] artists, String external_url_spotify) {
    this.name = name;
    this.artists = artists.clone();
    this.external_url_spotify = external_url_spotify;
  }

  @Override
  public String toString() {
    if (0 < artists.length) {
      return String.format("%s%n%s%n%s%n", name, Arrays.toString(artists), external_url_spotify);
    } else {
      return String.format("%s%n%s%n", name, external_url_spotify);
    }
  }
}