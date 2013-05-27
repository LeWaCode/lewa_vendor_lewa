package com.lewa.store.pkg;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.store.extras.GooglePackages;
import com.lewa.store.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class PkgManager {

    private static String TAG = PkgManager.class.getSimpleName();

    private Context context = null;

    private List<PackageInfo> packageInfos = null;
    private List<PackageInfo> userPackageInfos = null;
    private PackageManager packageManager = null;
    private List<HashMap<String, Object>> list = null;
    private List<PackageInfo> packs = null;
    private List<HashMap<String, Object>> userAppList = null;
    private List<String> specPackage = null;
    private GooglePackages gpk=null;

    public PkgManager(Context c) {
        this.context = c;
        new LoadAppsThread().start();
        this.packageManager = this.context.getPackageManager();
        this.specPackage = new ArrayList<String>();
        this.gpk=new GooglePackages();
    }

    // get User App
    public List<HashMap<String, Object>> getAppPackages() {
        list = new ArrayList<HashMap<String, Object>>();
        packs =packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);// 0获得所有的包括系统
        HashMap<String, Object> map = null;
        // 添加特殊处理包
        this.putSpecPackageApp();
        for (PackageInfo pi : packs) {
            if (((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) || ((pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)) {// 用户级应用
                map = new HashMap<String, Object>();
                // 得到所有安装的应用程序，包括系统应用程序
                map.put("icon",pi.applicationInfo.loadIcon(this.packageManager));// 图标
                map.put("appName",pi.applicationInfo.loadLabel(this.packageManager));// 应用程序名称
                map.put("packageName", pi.applicationInfo.packageName);// 应用程序包名
                map.put("appVersion", (pi.versionName != null) ? pi.versionName: "");
                map.put("appVersionCode", pi.versionCode);
                map.put("appDescripition",(pi.applicationInfo.loadDescription(this.packageManager) != null) ? pi.applicationInfo.loadDescription(this.packageManager) : "");// 介绍
                if (!list.contains(map)) {
                    list.add(map);
                }
            }else {//系统应用
                if (this.specPackage.contains(pi.applicationInfo.packageName)) {
                    map = new HashMap<String, Object>();
                    map.put("icon",pi.applicationInfo.loadIcon(this.packageManager));
                    map.put("appName",pi.applicationInfo.loadLabel(this.packageManager));
                    map.put("packageName", pi.applicationInfo.packageName);
                    map.put("appVersion",(pi.versionName != null) ? pi.versionName : "");
                    map.put("appVersionCode", pi.versionCode);
                    map.put("appDescripition",(pi.applicationInfo.loadDescription(this.packageManager) != null) ? pi.applicationInfo.loadDescription(this.packageManager) : "");// 介绍
                    if(!list.contains(map)){
                    	list.add(map);
                    }                    
//                  Log.d(TAG,"part of system application=="+map.toString());
                }
            }
            if (null != pi) {
                pi = null;
            }
        }
        if (null != packs) {
            packs.clear();
            packs = null;
        }
        return list;
    }

    private void putSpecPackageApp() {
        Map<String,String> fileMap=gpk.getGoogleSpecPackages();
        for(Map.Entry<String,String> entry:fileMap.entrySet()){
        	this.specPackage.add(entry.getKey());
        }
    }

    // Android自带安装
    // apkPath：apk完整路径:如/mnt/sdcard/1.apk
    public void installApk(String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)),
                "application/vnd.android.package-archive");
        this.context.startActivity(intent);
    }

    // Android自带卸载
    public void uninstallApk(String packageName) {
    	try {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
            this.context.startActivity(intent);
            // package:com.demo.CanavaCancel 这个形式是 package:程序完整的路径 (包名+程序名)
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    // 获得系统软件包
    public List<HashMap<String, Object>> getUserAppPackages() {
        userAppList = new ArrayList<HashMap<String, Object>>();
        // PackageManager pm = context.getPackageManager();
        HashMap<String, Object> map = null;
        for (PackageInfo pi : userPackageInfos) {
            map = new HashMap<String, Object>();
            // 得到所有安装的应用程序，包括系统应用程序
            map.put("icon", pi.applicationInfo.loadIcon(this.packageManager));// 图标
            map.put("appName",
                    pi.applicationInfo.loadLabel(this.packageManager));// 应用程序名称
            map.put("packageName", pi.applicationInfo.packageName);// 应用程序包名
            // 循环读取并存到HashMap中，再增加到ArrayList上，一个HashMap就是一项
            userAppList.add(map);
        }
        if (null != userPackageInfos) {
            userPackageInfos.clear();
            userPackageInfos = null;
        }
        return userAppList;
    }

    private void getAppsPackageInfos() {
        packageInfos = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    /**
     * 是否存在某个action
     * 
     * @param actionName
     * @return
     */
    public boolean isExistAction(String actionName) {
        Intent intent = new Intent(actionName);
        List<ResolveInfo> list = this.packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    // 加载App线程
    class LoadAppsThread extends Thread {
        @Override
        public void run() {
            getAppsPackageInfos();
            // 得到手机上安装的程序。
            userPackageInfos = new ArrayList<PackageInfo>();
            int size=packageInfos.size();
            for (int i = 0; i < size; i++) {
                PackageInfo temp = packageInfos.get(i);
                boolean isUserApp = false;
                ApplicationInfo appInfo = temp.applicationInfo;
                if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    // 表示是系统程序，但用户更新过，也算是用户安装的程序
                    isUserApp = true;
                } else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    // 一定是用户安装的程序
                    isUserApp = true;
                }
                if (isUserApp) {
                    userPackageInfos.add(temp);
                }
            }
            // 销毁packageInfos资源
            if (null != packageInfos) {
                packageInfos.clear();
                packageInfos = null;
            }
        }
    }

    /**
     * 调用framework.jar安装软件包
     * 
     * @param context
     * @param filePath
     */
    public static void packageInstall(Context context, String filePath) {

        Uri mPackageURI = Uri.fromFile(new File(filePath));
        int installFlags = 0;
        PackageManager pm = context.getPackageManager();
        /*
         * try { PackageInfo pi
         * =pm.getPackageInfo(packageName,PackageManager.GET_UNINSTALLED_PACKAGES
         * ); if(pi != null) { installFlags =0; } } catch(NameNotFoundException
         * e){ e.printStackTrace(); }
         */
        // PackageInstallObserver observer = new PackageInstallObserver();
        String s3 = context.getPackageName();
        // pm.installPackage(mPackageURI, observer, installFlags,s3);
    }

    /**
     * 若安装成功获取到的result值是"pkg: /data/local/tmp/Calculator.apk  \nSuccess"
     * 若失败，则没有结尾的Success
     * 
     * @param apkAbsolutePath
     * @return
     */
    public static boolean isInstalledSucess(String apkAbsolutePath) {
        // Log.i("PkgManager","install apk path=="+apkAbsolutePath);
        boolean flag = false;
        String str = "Success";
        String identifier="";
        if(installLocation==Constants.APP_INSTALL_AUTO){
        	identifier=installApkAuto(apkAbsolutePath);
        }else if(installLocation==Constants.APP_INSTALL_DEVICE){
        	identifier=installApkToDevice(apkAbsolutePath);
        }else if(installLocation==Constants.APP_INSTALL_SDCARD){
        	identifier=installPkgSdcard(apkAbsolutePath);
        }
        if (identifier.indexOf(str) != -1) {
            flag = true;
        }
        return flag;
    }
    
    public static String getAppInstallLocation(Context context) {
        int selectedLocation = Settings.System.getInt(context.getContentResolver(),Constants.DEFAULT_INSTALL_LOCATION, 0);
        if (selectedLocation == Constants.APP_INSTALL_DEVICE) {
        	installLocation=1;
            return Constants.APP_INSTALL_DEVICE_ID;
        } else if (selectedLocation == Constants.APP_INSTALL_SDCARD) {
        	installLocation=2;
            return Constants.APP_INSTALL_SDCARD_ID;
        } else  if (selectedLocation == Constants.APP_INSTALL_AUTO) {
        	installLocation=0;
            return Constants.APP_INSTALL_AUTO_ID;
        } else {
            // Default value, should not happen.
        	installLocation=0;
            return Constants.APP_INSTALL_AUTO_ID;
        }
    }
    
    //安装位置
    public static int installLocation=0;

    private static String setInstallProperty() {
    	Log.i(TAG,"installLocation code=="+installLocation);
        try {
            Process process = Runtime.getRuntime().exec("pm setInstallLocation "+installLocation);
            // Process process =
            // Runtime.getRuntime().exec("pm getInstallLocation");//返回2
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String runDelCommand(String path) {
        try {
            Process process = Runtime.getRuntime().exec("rm -rf "+path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            if (process.waitFor() != 0) {
                Log.e(TAG,"runDelCommand error,value=="+process.exitValue());
            }
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 后台安装软件包,安装到sd卡
     * 
     * @param apkAbsolutePath
     *            需要安装的apk文件路径
     * @return
     */
    public static String installPkgSdcard(String apkAbsolutePath) {

        String location = setInstallProperty();

        String result = "";
        String[] args = { "pm", "install", "-r", "-s", apkAbsolutePath };

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    
    /**
     * 后台安装软件包,安装到内存
     * 
     * @param apkAbsolutePath
     *            需要安装的apk文件路径
     * @return
     */
    public static String installApkToDevice(String apkAbsolutePath) {
    	Log.i(TAG,"installApkToDevice()");
    	
    	setInstallProperty();
    	
        String result = "";
        String[] args = { "pm", "install", "-r",apkAbsolutePath };

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    
    /**
     * 根据系统存储状态，自动决定安装位置
     * 
     * @param apkAbsolutePath
     *            需要安装的apk文件路径
     * @return
     */
    public static String installApkAuto(String apkAbsolutePath) {
    	Log.i(TAG,"installApkAuto()");
    	
    	setInstallProperty();
    	
        String result = "";
        String[] args = { "pm", "install", "-r",apkAbsolutePath };

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    
    /**
     * 软件包操作命令
     * @param args 命令行
     * @return
     */
    public static String runPackageCommand(String[] args) {
    	Log.d(TAG,"runPackageCommand()");
    	
        String result = "";

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    

    public static String execCommand(String command) throws IOException {
        // start the ls command running
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command); // 这句话就是shell与高级语言间的调用
        // 如果有参数的话可以用另外一个被重载的exec方法
        // 实际上这样执行时启动了一个子进程,它没有父进程的控制台
        // 也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            // System.out.println(line);
            sb.append(line);
            sb.append('\n');
        }
        // 使用exec执行不会等执行成功以后才返回,它会立即返回
        // 所以在某些情况下是很要命的(比如复制文件的时候)
        // 使用wairFor()可以等待命令执行完成以后才返回
        try {
            if (proc.waitFor() != 0) {//0表示正常终止
                System.err.println("exit value =" + proc.exitValue());
            }
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        return sb.toString();
    }

    /*
     * public List<HashMap<String, Object>> getUserAppPackages() {
     * List<HashMap<String, Object>> list = new ArrayList<HashMap<String,
     * Object>>(); List<PackageInfo> packs = pm
     * .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);//
     * 0获得所有的包括系统 for (PackageInfo pi : packs) { HashMap<String, Object> map =
     * new HashMap<String, Object>(); // 得到所有安装的应用程序，包括系统应用程序 map.put("icon",
     * pi.applicationInfo.loadIcon(pm));// 图标 map.put("appName",
     * pi.applicationInfo.loadLabel(pm));// 应用程序名称 map.put("packageName",
     * pi.applicationInfo.packageName);// 应用程序包名 //
     * 循环读取并存到HashMap中，再增加到ArrayList上，一个HashMap就是一项 list.add(map); } return
     * list; }
     */

}
