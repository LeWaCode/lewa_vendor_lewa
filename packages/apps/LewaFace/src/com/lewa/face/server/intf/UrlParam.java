/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.server.intf;

/**
 *
 * @author Administrator
 */
public class UrlParam extends NetBaseParam {

//        public UrlParam(String pageSize) {
//            this.pageSize = pageSize;
//        }
    public int pageSize;
    public String orderField = "";
    public String order = "";
    public int pageNo = 1;
    public final String CONST_PAGING = "_paging";
    public final String CONST_PAGE = "page";
    public final String CONST_COUNT = "_total";

    public UrlParam(String type) {
        super(type);
    }

    public static UrlParam newUrlParam(String type) {
        // TODO Auto-generated method stub
        UrlParam netBaseParam = new UrlParam(type);
        netBaseParam.mType = type;
        netBaseParam.screenSchema = NetBaseParam.getCurrentScreenSchema();
        netBaseParam.pageSize = ClientResolver.DEFAULT_PAGE_SIZE;
        netBaseParam.pageNo = 1;
        return netBaseParam;
    }

    @Override
    public String toString() {
//        if (pageSize == -1) {
            return super.toString();
//        }
//        return toPagingUrl();
    }

    public String toPagingUrl() {
        String resolutionParam;
        resolutionParam = resolveResolutionParam();
        String pagingStr = "";
        String pagingParam = "";
        if (pageSize != -1) {
            pagingStr = CONST_PAGING;
            if (pageNo != 1) {
                pagingParam = "&" + CONST_PAGE + "=" + pageNo;
            }
        }
        return NetBaseParam.PrefixUrl + "/" + mType + pagingStr + "?" + resolutionParam + pagingParam+"&"+CONST_SYS_VERSION+"="+NetBaseParam.SYS_VERSION;
    }

    public String toCountUrl() {
        String resolutionParam;
        resolutionParam = resolveResolutionParam();
        String countStr = CONST_COUNT;
        return NetBaseParam.PrefixUrl + "/" + mType + countStr + "?" + resolutionParam+"&"+CONST_SYS_VERSION+"="+NetBaseParam.SYS_VERSION;
    }
}
