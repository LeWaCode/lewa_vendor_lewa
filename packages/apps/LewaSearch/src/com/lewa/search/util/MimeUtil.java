package com.lewa.search.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This class was refered from file manager.
 * @author		wangfan
 * @version	2012.07.04
 */

public class MimeUtil {

    public static Map<String, String> filterMapByKey(Map<String, String> param, String key) {
        Map<String, String> copy = new HashMap<String, String>();
        for (Entry<String, String> en : param.entrySet()) {
            if (en.getValue().split("/")[0].contains(key)) {
                copy.put(en.getKey(), en.getValue());
            }
        }
        return copy.size() == 0 ? null : copy;
    }
}
