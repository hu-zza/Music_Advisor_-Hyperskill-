package advisor;

import java.util.ArrayList;
import java.util.HashMap;

abstract class Model
{
    private static final ArrayList<String> CATEGORIES_LIST = new ArrayList<>();     // category names in order
    private static final HashMap<String, String> CATEGORIES_MAP = new HashMap<>();  // K: name   V: spotify id

    static void clearAllCategory()
    {
        CATEGORIES_LIST.clear();
        CATEGORIES_MAP.clear();
    }

    static void addCategory(String id, String name)
    {
        CATEGORIES_LIST.add(name);
        CATEGORIES_MAP.put(name, id);
    }

    static String getCategoryId(String categoryName)
    {
        return CATEGORIES_MAP.getOrDefault(categoryName, "");
    }

    static ArrayList<String> getCategoryList()
    {
        return new ArrayList<>(CATEGORIES_LIST);
    }


    //K: spotify id   V: playlist array
    //arbitrary ids: "_newReleases_", "_featured_"
    private static final HashMap<String, ArrayList<MusicEntity>> CATEGORIES_PLAYLISTS = new HashMap<>();

    static void initializePlaylist(String playlistId)
    {
        CATEGORIES_PLAYLISTS.put(playlistId, new ArrayList<>());
    }

    static void addItemToPlaylist(String playlistId, MusicEntity item)
    {
        getPlaylistOrEmptyArray(playlistId).add(item);
    }

    static boolean containsPlaylist(String playlistId)
    {
        return CATEGORIES_PLAYLISTS.containsKey(playlistId);
    }

    static ArrayList<MusicEntity> getPlaylist(String playlistId)
    {
        System.out.println("getPlaylist");
        return new ArrayList<>(getPlaylistOrEmptyArray(playlistId));
    }

    static ArrayList<String> getStringPlaylist(String playlistId)
    {
        ArrayList<String> stringPlaylist = new ArrayList<>();

        for (MusicEntity item : getPlaylist(playlistId)) stringPlaylist.add(item.toString());

        return stringPlaylist;
    }

    private static ArrayList<MusicEntity> getPlaylistOrEmptyArray (String playlistId)
    {
        return CATEGORIES_PLAYLISTS.getOrDefault(playlistId, new ArrayList<>());
    }
}