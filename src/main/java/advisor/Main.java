package advisor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public abstract class Main
{
    // REMOTE SERVERS
    static String spotifyAccountServer;
    static String spotifyApiServer;

    // USER DATA
    static boolean authorized = false;
    static String codeForTokenRequest = "";
    static final HashMap<String, String> TOKEN = new HashMap<>(); // token renewal is not implemented

    // LOCAL DATA
    private static boolean waitingForUserInput = true;
    private static final String[] RESTRICTED_ACTIONS = {
            "new", "featured", "categories", "playlists", "view", "next", "prev"
    };
    private static final HashMap<String, String> ARGS_MAP = new HashMap<>();

    // FINAL STRINGS
    static final String CLIENT_ID = "";
    static final String CLIENT_SECRET = "";

    private static final String[] HELP_TEXT = {
            "Available commands before authorization:",
            "\tauth\t\t-\tLorem ipsum dolor sit amet...",
            "\thelp\t\t-\tLorem ipsum dolor sit amet...",
            "\texit\t\t-\tLorem ipsum dolor sit amet...",
            "",
            "Available commands after authorization:",
            "\tnew \t\t-\tLorem ipsum dolor sit amet...",
            "\tfeatured\t-\tLorem ipsum dolor sit amet...",
            "\tcategories\t-\tLorem ipsum dolor sit amet...",
            "\tplaylists\t-\tLorem ipsum dolor sit amet...",
            "\tview\t\t-\tLorem ipsum dolor sit amet...",
            "\tnext\t\t-\tLorem ipsum dolor sit amet...",
            "\tprev\t\t-\tLorem ipsum dolor sit amet...",
            ""
    };

    static
    {
        LocalHttpServer.start();

        //Arrays.binarySearch() need this:
        Arrays.sort(RESTRICTED_ACTIONS);
    }

    public static void main(String[] args)
    {
        initializationWithArgs(args);

        try (var br = new BufferedReader(new InputStreamReader(System.in)))
        {
            String cmd;
            while (waitingForUserInput && (cmd = br.readLine()) != null)
            {
                // cmdArr[0] - command, cmdArr[1] - optional parameters monolith
                String[] cmdArr = cmd.split(" ", 2);

                if (authorized || 0 > Arrays.binarySearch(RESTRICTED_ACTIONS, cmdArr[0]))
                {
                    selectMainFunction(cmdArr);
                }
                else
                {
                    System.out.println("Please, provide access for application.");
                    System.out.println("Shall I start authorization? [Y]es / [N]o");
                    String response = br.readLine();
                    if ("Y".equalsIgnoreCase(response) || "YES".equalsIgnoreCase(response))
                    {
                        selectMainFunction(new String[]{"auth"});
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Error at 'Main': " + e.getMessage());
        }
        LocalHttpServer.stop();
    }

    private static void initializationWithArgs(String[] args)
    {
        for (int x = 0; x < args.length / 2; x++) ARGS_MAP.put(args[2 * x], args[2 * x + 1]);

        spotifyAccountServer = ARGS_MAP.getOrDefault("-access", "https://accounts.spotify.com");
        spotifyApiServer = ARGS_MAP.getOrDefault("-resource", "https://api.spotify.com");
        Viewer.setItemsPerPage(Integer.parseInt(ARGS_MAP.getOrDefault("-page", "5")));
    }

    private static void selectMainFunction(String[] cmdArr)
    {
        switch (cmdArr[0])
        {
            case "new":
                System.out.println("---NEW RELEASES---");
                Controller.list(SpotifyList.NEW);
                break;

            case "featured":
                System.out.println("---FEATURED---");
                Controller.list(SpotifyList.FEATURED);
                break;

            case "categories":
                System.out.println("---CATEGORIES---");
                Controller.list(SpotifyList.CATEGORIES);
                break;

            case "playlists":
                if (cmdArr.length < 2)
                {
                    System.out.println("---ERROR---");
                    System.out.println("This command needs an argument: playlists <playlist name>");
                }
                else
                {
                    System.out.printf("---%S PLAYLISTS---%n", cmdArr[1]);
                    Controller.list(SpotifyList.PLAYLISTS, cmdArr[1]);
                }
                break;

            case "auth":
                System.out.println("---AUTHORIZATION---");
                Controller.authorization();
                break;

            case "view":
                Viewer.view();
                break;

            case "next":
                Viewer.next();
                break;

            case "prev":
                Viewer.previous();
                break;

            case "?":
            case "h":
            case "help":
                System.out.println("---HELP---");
                for (String line : HELP_TEXT) System.out.println(line);
                break;

            case "exit":
                System.out.println("---GOODBYE!---");
                waitingForUserInput = false;
                break;

            default:
                System.out.println("Unknown command. (Type 'help' for a list of commands.)");
                break;
        }
    }
}