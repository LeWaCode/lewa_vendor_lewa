package com.lewa.cit;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PhoneTestActivity extends Activity {
    /** Called when the activity is first created. */

    private static final String TAG = "PhoneTestAPP";
    private static final boolean DEBUG = false;

    private TextView phoneStateView;
    private String result = "";

    TelephonyManager telMgr = null;
    PhoneStateListener phoneStateLisnter = null;

    TextView phoneTypeView;
    TextView dataActivityView;
    TextView dataStateView;
    TextView networkCountryView;
    TextView networkOperatorIdView;
    TextView networkNameView;
    TextView networkTypeView;
    TextView simStateView;
    TextView simCountryView;
    TextView simOperatorCodeView;
    TextView simOperatorNameView;
    TextView simSerialView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phonetest);

        phoneTypeView = (TextView) findViewById(R.id.phone_type);
        dataActivityView = (TextView) findViewById(R.id.data_activity);
        dataStateView = (TextView) findViewById(R.id.data_state);
        networkCountryView = (TextView) findViewById(R.id.network_country);
        networkOperatorIdView = (TextView) findViewById(R.id.network_operator_id);
        networkNameView = (TextView) findViewById(R.id.network_name);
        networkTypeView = (TextView) findViewById(R.id.network_type);
        simStateView = (TextView) findViewById(R.id.sim_state);
        simCountryView = (TextView) findViewById(R.id.sim_country);
        simOperatorCodeView = (TextView) findViewById(R.id.sim_operator_code);
        simOperatorNameView = (TextView) findViewById(R.id.sim_operator_name);
        simSerialView = (TextView) findViewById(R.id.sim_serial);

        phoneStateView = (TextView) findViewById(R.id.phone_state);
        result = getResources().getString(R.string.phonestate);

        String srvcName = Context.TELEPHONY_SERVICE;
        telMgr = (TelephonyManager) getSystemService(srvcName);
        phoneStateLisnter = new TestPhoneStateListener();

        telMgr.listen(phoneStateLisnter, PhoneStateListener.LISTEN_CALL_STATE
                | PhoneStateListener.LISTEN_CELL_LOCATION
                | PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_DATA_ACTIVITY
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        
        int phoneType = telMgr.getPhoneType();
        switch (phoneType) {
        case (TelephonyManager.PHONE_TYPE_CDMA):
            phoneTypeView.setText("PHONE_TYPE_CDMA");
            break;
        case (TelephonyManager.PHONE_TYPE_GSM):
            phoneTypeView.setText("PHONE_TYPE_GSM");
            break;
        case (TelephonyManager.PHONE_TYPE_NONE):
            phoneTypeView.setText("PHONE_TYPE_NONE");
            break;
        default:
            break;
        }

        int dataActivity = telMgr.getDataActivity();
        int dataState = telMgr.getDataState();
        switch (dataActivity) {
        case (TelephonyManager.DATA_ACTIVITY_DORMANT):
            dataActivityView.setText("DATA_ACTIVITY_DORMANT");
            break;
        case (TelephonyManager.DATA_ACTIVITY_IN):
            dataActivityView.setText("DATA_ACTIVITY_IN");
            break;
        case (TelephonyManager.DATA_ACTIVITY_INOUT):
            dataActivityView.setText("DATA_ACTIVITY_INOUT");
            break;
        case (TelephonyManager.DATA_ACTIVITY_OUT):
            dataActivityView.setText("DATA_ACTIVITY_OUT");
            break;
        case (TelephonyManager.DATA_ACTIVITY_NONE):
            dataActivityView.setText("DATA_ACTIVITY_NONE");
            break;
        default:
            break;
        }

        switch (dataState) {
        case (TelephonyManager.DATA_CONNECTED):
            dataStateView.setText("DATA_CONNECTED");
            break;
        case (TelephonyManager.DATA_CONNECTING):
            dataStateView.setText("DATA_CONNECTING");
            break;
        case (TelephonyManager.DATA_DISCONNECTED):
            dataStateView.setText("DATA_DISCONNECTED");
            break;
        case (TelephonyManager.DATA_SUSPENDED):
            dataStateView.setText("DATA_SUSPENDED");
            break;
        }

        String networkCountry = telMgr.getNetworkCountryIso();
        networkCountryView.setText(networkCountry);

        String networkOperatorId = telMgr.getNetworkOperator();
        networkOperatorIdView.setText(networkOperatorId);

        String networkName = telMgr.getNetworkOperatorName();
        networkNameView.setText(networkName);

        int networkType = telMgr.getNetworkType();
        switch (networkType) {
        case (TelephonyManager.NETWORK_TYPE_1xRTT):
            networkTypeView.setText("NETWORK_TYPE_1xRTT");
            break;
        case (TelephonyManager.NETWORK_TYPE_CDMA):
            networkTypeView.setText("NETWORK_TYPE_CDMA");
            break;
        case (TelephonyManager.NETWORK_TYPE_EDGE):
            networkTypeView.setText("NETWORK_TYPE_EDGE");
            break;
        case (TelephonyManager.NETWORK_TYPE_EVDO_0):
            networkTypeView.setText("NETWORK_TYPE_EVDO_0");
            break;
        case (TelephonyManager.NETWORK_TYPE_EVDO_A):
            networkTypeView.setText("NETWORK_TYPE_EVDO_A");
            break;
        case (TelephonyManager.NETWORK_TYPE_EVDO_B):
            networkTypeView.setText("NETWORK_TYPE_EVDO_B");
            break;
        case (TelephonyManager.NETWORK_TYPE_GPRS):
            networkTypeView.setText("NETWORK_TYPE_GPRS");
            break;
        case (TelephonyManager.NETWORK_TYPE_HSDPA):
            networkTypeView.setText("NETWORK_TYPE_HSDPA");
            break;
        case (TelephonyManager.NETWORK_TYPE_HSPA):
            networkTypeView.setText("NETWORK_TYPE_HSPA");
            break;
        case (TelephonyManager.NETWORK_TYPE_HSUPA):
            networkTypeView.setText("NETWORK_TYPE_HSUPA");
            break;
        case (TelephonyManager.NETWORK_TYPE_IDEN):
            networkTypeView.setText("NETWORK_TYPE_IDEN");
            break;
        case (TelephonyManager.NETWORK_TYPE_UMTS):
            networkTypeView.setText("NETWORK_TYPE_UMTS");
            break;
        case (TelephonyManager.NETWORK_TYPE_UNKNOWN):
            networkTypeView.setText("NETWORK_TYPE_UNKNOWN");
            break;
        default:
            break;
        }

        int simState = telMgr.getSimState();
        switch (simState) {
        case (TelephonyManager.SIM_STATE_ABSENT):
            simStateView.setText("SIM_STATE_ABSENT");
            break;
        case (TelephonyManager.SIM_STATE_NETWORK_LOCKED):
            simStateView.setText("SIM_STATE_NETWORK_LOCKED");
            break;
        case (TelephonyManager.SIM_STATE_PIN_REQUIRED):
            simStateView.setText("SIM_STATE_PIN_REQUIRED");
            break;
        case (TelephonyManager.SIM_STATE_PUK_REQUIRED):
            simStateView.setText("SIM_STATE_PUK_REQUIRED");
            break;
        case (TelephonyManager.SIM_STATE_READY): {
            simStateView.setText("SIM_STATE_READY");

            String simCountry = telMgr.getSimCountryIso();
            simCountryView.setText(simCountry);

            String simOperatorCode = telMgr.getSimOperator();
            simOperatorCodeView.setText(simOperatorCode);

            String simOperatorName = telMgr.getSimOperatorName();
            simOperatorNameView.setText(simOperatorName);

            String simSerial = telMgr.getSimSerialNumber();
            simSerialView.setText(simSerial);

        }
            break;
        case (TelephonyManager.SIM_STATE_UNKNOWN):
            simStateView.setText("SIM_STATE_UNKNOWN");
            break;
        default:
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        telMgr.listen(phoneStateLisnter, PhoneStateListener.LISTEN_NONE);
        Log.v(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    class TestPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.e(TAG, "CALL_STATE_IDLE");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.e(TAG, "CALL_STATE_RINGING AND Number: " + incomingNumber);
                // String.format(result , tmp);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.e(TAG, "CALL_STATE_OFFHOOK");
            default:
                break;
            }
            phoneStateView.setText(result + "TAG " + TAG + " Show in the Log");
            super.onCallStateChanged(state, incomingNumber);
        }

        public void onCellLocationChanged(CellLocation location) {
            GsmCellLocation gsmLocation = (GsmCellLocation) location;
            Toast.makeText(getApplicationContext(),
                    String.valueOf(gsmLocation.getCid()), Toast.LENGTH_LONG)
                    .show();
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                String toastText = serviceState.getOperatorAlphaLong();
                Toast.makeText(getApplicationContext(), toastText,
                        Toast.LENGTH_SHORT).show();
            } else if (serviceState.getState() == ServiceState.STATE_EMERGENCY_ONLY) {
                Toast.makeText(getApplicationContext(), "STATE_EMERGENCY_ONLY",
                        Toast.LENGTH_SHORT).show();
            } else if (serviceState.getState() == ServiceState.STATE_OUT_OF_SERVICE) {
                Toast.makeText(getApplicationContext(), "STATE_OUT_OF_SERVICE",
                        Toast.LENGTH_SHORT).show();
            } else if (serviceState.getState() == ServiceState.STATE_POWER_OFF) {
                Toast.makeText(getApplicationContext(), "STATE_POWER_OFF",
                        Toast.LENGTH_SHORT).show();
            }
        }

        public void onDataActivity(int direction) {
            switch (direction) {
            case (TelephonyManager.DATA_ACTIVITY_DORMANT):
                dataActivityView.setText("DATA_ACTIVITY_DORMANT");
                break;
            case (TelephonyManager.DATA_ACTIVITY_IN):
                dataActivityView.setText("DATA_ACTIVITY_IN");
                break;
            case (TelephonyManager.DATA_ACTIVITY_INOUT):
                dataActivityView.setText("DATA_ACTIVITY_INOUT");
                break;
            case (TelephonyManager.DATA_ACTIVITY_OUT):
                dataActivityView.setText("DATA_ACTIVITY_OUT");
                break;
            case (TelephonyManager.DATA_ACTIVITY_NONE):
                dataActivityView.setText("DATA_ACTIVITY_NONE");
                break;
            default:
                break;
            }
        }

        public void onDataConnectionStateChanged(int state) {
            switch (state) {
            case (TelephonyManager.DATA_CONNECTED):
                dataStateView.setText("DATA_CONNECTED");
                break;
            case (TelephonyManager.DATA_CONNECTING):
                dataStateView.setText("DATA_CONNECTING");
                break;
            case (TelephonyManager.DATA_DISCONNECTED):
                dataStateView.setText("DATA_DISCONNECTED");
                break;
            case (TelephonyManager.DATA_SUSPENDED):
                dataStateView.setText("DATA_SUSPENDED");
                break;
            }
        }
    }
}
