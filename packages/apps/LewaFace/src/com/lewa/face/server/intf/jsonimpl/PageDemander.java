/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf.jsonimpl;

import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.server.intf.NetBaseParam;
import com.lewa.face.server.intf.NetHelper;
import com.lewa.face.server.intf.PageResolver;
import com.lewa.face.server.intf.UrlParam;
import com.lewa.face.util.Logs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class PageDemander extends PageResolver {

    List whole = new ArrayList(0);
    List curr = new ArrayList();

    public PageDemander(int pageSize) {
        super(pageSize);
    }


    @Override
    public List currentPage() {
        if (pageSize == -1) {
            curr = getWhole(this.clientResolver.url);
            return curr;
        }
        return LewaServerJsonParser.parseListThemeBase(NetHelper.getNetString(this.clientResolver.url.toString()), NetBaseParam.isPackgeResource(this.clientResolver.url.mType));
    }

    @Override
    public List nextPage() {
        pageNo++;
        curr = currentPage();
        whole.addAll(curr);
        return curr;
    }

    @Override
    public void init() {
    }

    @Override
    public void requestList() {
    }

    /**
     * 
     * @param ifneeded 传NetBaseParam
     * @return 
     */
    @Override
    public boolean count(Object... ifneeded) {
        Logs.i("========= string "+NetHelper.getNetString(((UrlParam) ifneeded[0]).toCountUrl()));
        this.totalCount = LewaServerJsonParser.parseCount(NetHelper.getNetString(((UrlParam) ifneeded[0]).toCountUrl()));
        resetTotalCount(this.totalCount);
        Logs.i("--- getPageCount " + this.getPageCount());
        return true;
    }

    @Override
    public List getRequestedEntities() {
        return curr;
    }

    public ThemeBase addThumbNPreviews(ThemeBase themebase, String cntStr) {

        return themebase;
    }

    @Override
    public List pretPages(int n) {
        this.clear();
        for (int i = 1; i <= n; i++) {
            this.setPageNo(i);
            this.whole.addAll(this.currentPage());
        }
        return this.whole;
    }

    @Override
    public void setPageNo(int pageNo) {
        super.setPageNo(pageNo);
        ((UrlParam) this.clientResolver.url).pageNo = pageNo;
    }

    private List getWhole(String url) {
    	Logs.i("========="+url);
        return LewaServerJsonParser.parseListThemeBase(NetHelper.getNetString(url), NetBaseParam.isPackgeResource(this.clientResolver.url.actualtype),this.clientResolver.url);
    }

    private List getWhole(NetBaseParam url) {
        return getWhole(url.toString());
    }

    public void clear() {
        whole.clear();
        curr.clear();
    }
}
