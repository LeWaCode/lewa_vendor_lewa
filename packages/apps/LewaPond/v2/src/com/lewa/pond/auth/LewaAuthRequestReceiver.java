package com.lewa.pond.auth;

import com.lewa.core.base.LewaComponentCache;
import com.lewa.core.base.LewaContentDAO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LewaAuthRequestReceiver extends BroadcastReceiver {
	private static String TAG = "LewaAuthRequestReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction().toString();
		if (action.equals("com.lewa.auth.action.queryclientdata")) {
//            Log.d(TAG, "query client data");
            LewaContentDAO dao = (LewaContentDAO) LewaComponentCache.getInstance(context).getComponent(LewaComponentCache.COMPONENT_DAO);
            String cid = dao.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_CLIENT_ID);
            String uid = dao.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_USER_ID);
            String returnAction = intent.getExtras().getString("action");
            Intent resultIntent = new Intent(returnAction);
            resultIntent.setAction(returnAction);
            resultIntent.putExtra("cid", cid);
            resultIntent.putExtra("uid", uid);
//            Log.d(TAG, "sending broadcast for query client response having action: " + returnAction);
            context.sendBroadcast(resultIntent);
//            Log.d("LewaBroadcastReceiver", "sendBroadcast : com.lewa.pim.action.responseaccount");
        }
	}

}
