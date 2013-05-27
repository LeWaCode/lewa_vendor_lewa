package com.android.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class GoogleAppActivity extends ListActivity {
    private static IPackageManager sPM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sPM = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        String[] pkgNames = getResources().getStringArray(R.array.google_service_package);
        List<HashMap<String, String>> maps = new ArrayList<HashMap<String,String>>(pkgNames.length);
        for (int i = 0; i < pkgNames.length; i++) {
            if (isApkExist(pkgNames[i])) {
                ApplicationInfo info = null;
                try {
                    info = getPackageManager().getApplicationInfo(pkgNames[i], 0);
                } catch (NameNotFoundException e) {
                    continue;
                }
                if (info != null) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("app_name", info.loadLabel(getPackageManager()).toString());
                    map.put("pkg_name", pkgNames[i]);
                    maps.add(map);
                }
            }
        }
        getListView().setCacheColorHint(0);
        setListAdapter(new AppAdapter(this, maps, 0, null, null));
        
        if (maps.size() == 0) {
            Toast.makeText(this, getString(R.string.no_google_apps), Toast.LENGTH_SHORT).show();     
        }
    }
    
    public boolean isApkExist(String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
        }
        return (packageInfo == null ? false : true);
    }
    
    class AppAdapter extends SimpleAdapter {
        private static final String GSF_PKG = "com.google.android.gsf";
        private Context mContext;
        private List<HashMap<String, String>> mList;
        private String gsfTag;

        public AppAdapter(Context context, List<HashMap<String, String>> data,
                int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            
            mContext = context;
            mList = data;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(android.R.layout.select_dialog_multichoice, null);
            }

            CheckedTextView ctv = (CheckedTextView)convertView.findViewById(android.R.id.text1);
            HashMap<String, String> info = mList.get(position);
            String appName = info.get("app_name");
            String pkgName = info.get("pkg_name");
            if (pkgName.equalsIgnoreCase(GSF_PKG)) {
                gsfTag = appName+"-"+pkgName;
            }
            ctv.setText(appName);
            int AppEnabledState = -1;
            try {
                AppEnabledState = sPM.getApplicationEnabledSetting(pkgName);
            } catch (RemoteException e) {
                AppEnabledState = -1;
            }
            ctv.setChecked(AppEnabledState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
            ctv.setOnClickListener(listener);
            ctv.setTag(appName+"-"+pkgName);
            return convertView;
        }
        
        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String tag = (String)v.getTag();
                int index = tag.indexOf("-");
                final String appName = tag.substring(0, index);
                final String pkgName = tag.substring(index + 1, tag.length());
                // the check status is the status of the checkedtextview before clicked
                final boolean isChecked = ((CheckedTextView)v).isChecked();
                if (pkgName.equalsIgnoreCase(GSF_PKG)) {
                    if (isChecked) {
                        new AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.enable_app_title, appName))
                        .setMessage(getString(R.string.enable_gsf_message))
                        .setPositiveButton(R.string.enable_gsf, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setState(pkgName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, v);
                            }
                        })
                        .setNeutralButton(R.string.enable_all_apps, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setAllStatus(v, false);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                    } else {
                        new AlertDialog.Builder(mContext)
                        .setTitle(getString(R.string.disable_app_title, appName))
                        .setMessage(getString(R.string.disable_gsf_message))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setAllStatus(v, true);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                    }
                } else {
                    if (isAppDepondsOnGsf(pkgName)) {
                        int gsfState = -1;
                        try {
                            gsfState = sPM.getApplicationEnabledSetting(GSF_PKG);
                        } catch (RemoteException e) {
                            gsfState = -1;
                        } catch (IllegalArgumentException e2) {
                            Toast.makeText(mContext, getString(R.string.gsf_not_installed_warning), Toast.LENGTH_SHORT).show();      // gsf is not installed
                            return;
                        }
                        
                        boolean isGsfEnabled = (gsfState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
                        if (!isGsfEnabled) {
                            if (isChecked) {
                                new AlertDialog.Builder(mContext)
                                .setTitle(getString(R.string.enable_app_title, appName))
                                .setMessage(getString(R.string.enable_other_message))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        View ct = v.getRootView().findViewWithTag(gsfTag);
                                        setState(GSF_PKG, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, ct);
                                        setState(pkgName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, v);  
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                            }                          
                        } else {
                            setState(pkgName, isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, v);
                        } 
                        
                    } else {
                        setState(pkgName, isChecked ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, v); 
                    }
                }
            }
        };
        
        private void setState(String packageName, int state, View v) {
            try {
                sPM.setApplicationEnabledSetting(packageName, state, PackageManager.DONT_KILL_APP);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ((CheckedTextView) v).toggle();
        }
        
        private boolean isAppDepondsOnGsf(String pkgName) {
            String[] notDependsOnGsf = getResources().getStringArray(R.array.not_depends_gsf_list);
            for (String s : notDependsOnGsf) {
                if (s.equalsIgnoreCase(pkgName)) {
                    return false;
                }
            }
            return true;
        }
        
        private void setAllStatus(View v, boolean bEnabled) {
            try {
                for (int i = 0; i < mList.size(); i++) {
                    String appName = mList.get(i).get("app_name");
                    String pkgName = mList.get(i).get("pkg_name");
                    String tagStr = appName+"-"+pkgName; 
                    CheckedTextView ct = (CheckedTextView) v.getRootView().findViewWithTag(tagStr);
                    sPM.setApplicationEnabledSetting(
                            pkgName,
                            !bEnabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            1);
                    ct.setChecked(bEnabled);
                }
            } catch (RemoteException e) {
                
            }
        }
    }
}