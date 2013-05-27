package com.tencent.tmsecure.aresengine.dao;

import java.util.List;
import android.util.Log;
import com.tencent.tmsecure.module.aresengine.ContactEntity;

public class DaoHelper {
    public static void populateStaticData(List<ContactEntity> contactList,
        int numEntities, int entityIds[], String phoneNums[], String names[]) {
        contactList.clear();
        for (int i = 0; i < numEntities; ++i) {
            ContactEntity entity = new ContactEntity();
            entity.id = entityIds[i];
            entity.phonenum = phoneNums[i];
            entity.name = names[i];
            contactList.add(entity);
        }
    }

    public static boolean isInBlackList(List<ContactEntity> contactList, String phonenum, int callfrom) {
        for (ContactEntity entity : contactList) {
            String pattern = entity.phonenum;
            if (pattern.length() > 8) {
                pattern = pattern.substring(pattern.length() - 8);
            }

            if (phonenum.endsWith(pattern)) {
                //0: CALL_FROM_CALLFILTER 1: CALL_FROM_SMSFILTER 
                return callfrom == 0 ? entity.enableForCalling : entity.enableForSMS; 
            }
        }
        return false;
    }

    public static boolean contains(List<ContactEntity> contactList,
            String phonenum, int callfrom) {
        for (ContactEntity entity : contactList) {
            String pattern = entity.phonenum;
            if (pattern.length() > 8) {
                pattern = pattern.substring(pattern.length() - 8);
            }
            if (phonenum!=null && phonenum.endsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
