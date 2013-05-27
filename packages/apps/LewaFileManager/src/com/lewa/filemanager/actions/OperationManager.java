/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions;

import com.lewa.app.filemanager.ui.CommonActivity;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class OperationManager {

    public static Map<OperationUtil, CommonActivity> operationMap = new HashMap<OperationUtil, CommonActivity>();
    public static OperationUtil operationTaker;

    public static void clearMarks() {
        OperationUtil.setOperType(-1);
        OperationUtil.dataClear();
//        if (!operationMap.containsKey(operationTaker)) {
//            throw new IllegalStateException("wrong taker's info");
//        }
//        operationMap.get(operationTaker).clearUISelect();
        OperationManager.operationTaker = null;
        OperationUtil.selectedActivity = null;
        OperationUtil.operMatcher.clear();
    }
}
