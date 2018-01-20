package io.github.notsyncing.lightfur.core.models;

import io.github.notsyncing.lightfur.core.utils.PageUtils;

import java.util.List;

public class PageResult<T>
{
    private List<T> list;
    private int pageNum;
    private int pageSize;
    private int pageCount;
    private long totalCount;

    public PageResult()
    {

    }

    public PageResult(List<T> list, int pageNum, int pageSize, int pageCount, int totalCount)
    {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageCount = pageCount;
        this.totalCount = totalCount;
    }

    public PageResult(List<T> list, int pageNum, int pageSize, int totalCount)
    {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageCount = PageUtils.calculatePageCount(pageSize, totalCount);
        this.totalCount = totalCount;
    }

    public List<T> getList()
    {
        return list;
    }

    public void setList(List<T> list)
    {
        this.list = list;
    }

    public int getPageNum()
    {
        return pageNum;
    }

    public void setPageNum(int pageNum)
    {
        this.pageNum = pageNum;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public int getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;
    }

    public long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(long totalCount)
    {
        this.totalCount = totalCount;
        this.pageCount = PageUtils.calculatePageCount(pageSize, totalCount);
    }
}
