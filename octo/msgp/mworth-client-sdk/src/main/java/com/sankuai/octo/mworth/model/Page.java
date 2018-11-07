package com.sankuai.octo.mworth.model;

public class Page implements Cloneable {
    public static int DEFAULT_PAGESIZE = 20;
    public static int DEFAULT_PAGE = 1;
    private int pageNo = DEFAULT_PAGE;
    private int pageSize = DEFAULT_PAGESIZE;
    private int totalCount = -1;
    private int totalPageCount = 1;

    public Page() {
    }

    public Page(Integer pageNo) {
        this.pageNo = pageNo;
        this.pageSize = DEFAULT_PAGESIZE;
    }

    public Page(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize == null ? DEFAULT_PAGESIZE : pageSize;
    }

    public Page(Integer pageNo, Integer pageSize, Integer totalCount) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public int getStart() {
        if (pageNo < 0 || pageSize < 0) {
            return -1;
        } else {
            return ((pageNo - 1) * pageSize);
        }
    }

    public int getTotalPageCount() {
        calculateTotalPageCount();
        return totalPageCount;
    }

    public void calculateTotalPageCount() {
        totalPageCount = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            totalPageCount++;
        }

        // 校正页码
        if (pageNo > totalPageCount) {
            pageNo = totalPageCount;
        }
        if (pageNo < 1) {
            pageNo = 1;
        }
    }

    public boolean isHasNextPage() {
        return (pageNo + 1 <= getTotalPageCount());
    }

    public int getNextPage() {
        if (isHasNextPage())
            return pageNo + 1;
        else
            return pageNo;
    }

    public boolean isHasPrePage() {
        return (pageNo - 1 >= 1);
    }

    public int getPrePage() {
        if (isHasPrePage())
            return pageNo - 1;
        else
            return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int page) {
        this.pageNo = page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        // 计算总页数
        calculateTotalPageCount();
    }

    @Override
    public Object clone() {
        Object page = null;
        try {
            page = super.clone();
        } catch (CloneNotSupportedException e) {
        }
        return page;
    }
}
