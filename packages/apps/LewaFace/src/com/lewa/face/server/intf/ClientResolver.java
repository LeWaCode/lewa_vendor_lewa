/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf;

import com.lewa.face.server.intf.jsonimpl.PageDemander;
import com.lewa.face.server.intf.xlsimpl.PageLocalParser;

/**
 *
 * @author chenliang
 */
public class ClientResolver {

    public NetBaseParam url;
    public static final int XLS_IMPL = 0;
    public static final int JSON_IMPL = 1;
    public PageResolver pageResolver;
    int implType = -1;
    int pageSize;
    public static final int DEFAULT_PAGE_SIZE = -1;

    public ClientResolver(NetBaseParam url, int implType, int pageSize) {
        this.url = url;
        this.implType = implType;
        this.pageSize = pageSize;
        switch (implType) {
            case XLS_IMPL:
                pageResolver = new PageLocalParser(pageSize);
                break;
            case JSON_IMPL:
                pageResolver = new PageDemander(pageSize);
                break;
        }
//        pageResolver.parsePackage = url.type.trim().equalsIgnoreCase(NetBaseParam.THEMEPACKAGE);
        pageResolver.setClientResolver(this);
    }

    public boolean count(Object... objs) {
        return pageResolver.count(objs);
    }

    public String getUrl() {
        return url.toString();
    }

    public PageResolver getPageResolver() {
        return pageResolver;
    }
}
