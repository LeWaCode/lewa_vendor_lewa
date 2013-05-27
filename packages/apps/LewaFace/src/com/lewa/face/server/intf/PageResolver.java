/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf;

import java.util.List;

/**
 *
 * @author chenliang
 */
public abstract class PageResolver {

    public int totalCount;
    public int pageCount;
    public int pageSize;//当前只分1页，无限记录
    public int pageNo = 1;
    public ClientResolver clientResolver;
    public boolean parsePackage;

    public PageResolver(int pageSize) {
        resetPageSize(pageSize);
    }

    public final void setClientResolver(ClientResolver clientResolver) {
        this.clientResolver = clientResolver;
    }

    public abstract void requestList();

    public abstract List getRequestedEntities();

    public abstract boolean count(Object... ifneeded);

    /**
     * 获得每页的记录数量,无默认值.
     */
    public int getPageSize() {
        return pageSize;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public final void resetPageSize(int pageSize) {
        this.pageSize = pageSize;
        updatePagination();
    }

    public void resetTotalCount(int count) {
        totalCount = count;
        updatePagination();
    }

    /**
     * 计算总页数.
     */
    public final void updatePagination() {
        if (totalCount == -1) {
            pageCount = 0;
        }
        int count = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            count++;
        }
        pageCount = count;
    }

    /**
     * 是否还有下一页.
     */
    public boolean hasNext() {
        return (pageNo + 1 <= this.getPageCount());
    }

    /**
     * 是否还有上一页. 
     */
    public boolean hasPre() {
        return (pageNo - 1 >= 1);
    }

    /**
     * 返回上页的页号,序号从1开始.
     */
    public int getPrePage() {
        if (hasPre()) {
            return pageNo - 1;
        } else {
            return pageNo;
        }
    }

    /**
     * 返回下页的页号,序号从1开始.
     */
    public int getNextPage() {
        if (hasNext()) {
            return pageNo + 1;
        } else {
            return pageNo;
        }
    }

    /**
     * 获得当前页的页号,序号从1开始,默认为1.
     */
    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        
        this.pageNo = pageNo;
    }

    public abstract List pretPages(int n);

    public abstract List currentPage();

    public abstract List nextPage();

    public abstract void init();
}
