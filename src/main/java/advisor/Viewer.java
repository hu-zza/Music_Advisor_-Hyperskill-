package advisor;

import java.util.List;

abstract class Viewer
{
    private static String[] list = new String[0];
    private static int itemsPerPage = 5;
    private static int offset = 0;

    static void setList(List<String> newList)
    {
        list = newList.toArray(new String[0]);
        offset = 0;
    }

    static void setItemsPerPage(int count)
    {
        itemsPerPage = 0 < count ? count : 1;
    }

    static void view()
    {
        if (list.length == 0)
        {
            System.out.println("Nothing to show.");
            return;
        }

        int max = Math.min(list.length, offset + itemsPerPage);

        for (int x = offset; x < max; x++) System.out.println(list[x]);

        System.out.printf("---PAGE %d OF %d---%n",
                          (int) Math.ceil((offset + 1.0) / itemsPerPage),
                          (int) Math.ceil((double) list.length / itemsPerPage)
        );
    }

    static void next()
    {
        if (list.length <= offset + itemsPerPage)
        {
            System.out.println("No more pages.");
        }
        else
        {
            offset += itemsPerPage;
            view();
        }
    }

    static void previous()
    {
        if (0 > offset - itemsPerPage)
        {
            System.out.println("No more pages.");
        }
        else
        {
            offset -= itemsPerPage;
            view();
        }
    }
}