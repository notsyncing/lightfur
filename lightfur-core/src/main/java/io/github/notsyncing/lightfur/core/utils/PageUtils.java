package io.github.notsyncing.lightfur.core.utils;

public class PageUtils
{
    public static int calculatePageCount(int pageSize, long totalCount)
    {
        if (totalCount == 0) {
            return 0;
        }

        if (pageSize <= 0) {
            return 0;
        }

        if (totalCount < pageSize) {
            return 1;
        }

        int pageCount = (int)(totalCount / pageSize);

        if (totalCount % pageSize > 0) {
            pageCount++;
        }

        return pageCount;
    }
}
