package kr.co.surveylink.www.mobilelink;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jsyang on 2016-09-02.
 */
public class MInstalledApp {

    static public List<HashMap<String,String>> getInstalledApp(Context context) {
        Common.log("MInstalledApp getInstalledApp called");
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        List<HashMap<String,String>> list = new ArrayList<>();
        HashMap<String,String> ret;
        for (Object obj : pkgAppsList) {
            ret = new HashMap<String,String>();
            ResolveInfo resolveInfo = (ResolveInfo) obj;
            PackageInfo packageInfo = null;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            int i;
            String packageName = "";
            String permissions = "";
            String applicationLabel = "";
            long firstInstallTime = 0;
            long lastUpdateTime = 0;
            int versionCode = 0;
            String versionName = "";

            packageName=Common.isNull(packageInfo.packageName);
            if(packageInfo.requestedPermissions !=null){
                for(i=0;i<packageInfo.requestedPermissions.length;i++){
                    permissions+="|"+packageInfo.requestedPermissions[i];
                }
                permissions=permissions.substring(1);
            }
            applicationLabel=Common.isNull(context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString());
            firstInstallTime=packageInfo.firstInstallTime;
            lastUpdateTime=packageInfo.lastUpdateTime;
            versionCode=packageInfo.versionCode;
            versionName=packageInfo.versionName;

            ret.put("packageName",packageName);
            ret.put("permissions",permissions);
            ret.put("applicationLabel",applicationLabel);
            ret.put("firstInstallTime",Long.toString(firstInstallTime));
            ret.put("lastUpdateTime",Long.toString(lastUpdateTime));
            ret.put("versionCode",Integer.toString(versionCode));
            ret.put("versionName",versionName);

            Common.log("----------------------");
            Common.log( "packageName : "+packageName);//com.nhn.android.search
            Common.log( "permissions : "+permissions);//android.permission.INTERNET|android.permission.ACCESS_WIFI_STATE|android.permission.CHANGE_WIFI_STATE|android.permission.ACCESS_NETWORK_STATE|android.permission.CHANGE_NETWORK_STATE|android.permission.READ_CONTACTS|android.permission.WRITE_CONTACTS|android.permission.ACCESS_FINE_LOCATION|android.permission.ACCESS_COARSE_LOCATION|android.permission.RECORD_AUDIO|android.permission.MODIFY_AUDIO_SETTINGS|android.permission.WRITE_EXTERNAL_STORAGE|android.permission.READ_EXTERNAL_STORAGE|android.permission.CAMERA|android.permission.CALL_PHONE|android.permission.READ_PHONE_STATE|android.permission.GET_TASKS|android.permission.SYSTEM_ALERT_WINDOW|android.permission.PACKAGE_USAGE_STATS|android.permission.WAKE_LOCK|android.permission.EXPAND_STATUS_BAR|android.permission.GET_ACCOUNTS|android.permission.USE_CREDENTIALS|android.permission.MANAGE_ACCOUNTS|android.permission.AUTHENTICATE_ACCOUNTS|android.permission.KILL_BACKGROUND_PROCESSES|android.permission.GET_PACKAGE_SIZE|android.permission.RECEIVE_BOOT_COMPLETED|android.permission.VIBRATE|com.android.launcher.permission.INSTALL_SHORTCUT|com.nhn.android.search.permission.C2D_MESSAGE|com.google.android.c2dm.permission.RECEIVE|com.nhn.android.search.permission.NNI_MESSAGE|org.fidoalliance.uaf.permissions.FIDO_CLIENT|android.permission.INJECT_EVENT
            Common.log( "applicationLabel : "+applicationLabel);//NAVER
            Common.log( "firstInstallTime : "+Long.toString(firstInstallTime));//1472777667317
            Common.log( "lastUpdateTime : "+Long.toString(lastUpdateTime));//1472777667317
            Common.log( "versionCode : "+Integer.toString(versionCode));//12
            Common.log( "versionName : "+versionName);//1.1

            /*
            if(packageInfo.activities != null) {
                Common.log("activities : ");
                for (ActivityInfo t1 : packageInfo.activities) {
                    Common.log(t1.name);
                }
            }
            Common.log("processName : ");
            Common.log(Common.isNull(packageInfo.applicationInfo.processName));
            Common.log("taskAffinity : ");
            Common.log(Common.isNull(packageInfo.applicationInfo.taskAffinity));
            if(packageInfo.providers != null) {
                Common.log("providers : ");
                for (ProviderInfo t2 : packageInfo.providers) {
                    Common.log(t2.toString());
                }
            }
            if(packageInfo.receivers != null) {
                Common.log("receivers : ");
                for (ActivityInfo t3 : packageInfo.receivers) {
                    Common.log(t3.toString());
                }
            }
            if(packageInfo.reqFeatures != null) {
                Common.log("reqFeatures : ");
                for (FeatureInfo t4 : packageInfo.reqFeatures) {
                    Common.log(t4.toString());
                }
            }
            if(packageInfo.services != null) {
                Common.log("services : ");
                for (ServiceInfo t5 : packageInfo.services) {
                    Common.log(t5.toString());
                }
            }
            if(packageInfo.sharedUserId !=null ) {
                Common.log( "sharedUserId : ");
                Common.log( packageInfo.sharedUserId);
            }
            if(Build.VERSION.SDK_INT>=21) {
                if (packageInfo.splitNames != null) {
                    Common.log( "\tsplitNames : ");
                    for (String t6 : packageInfo.splitNames) {
                        Common.log( t6);
                    }
                }
            }
            */
            list.add(ret);
        }
        return list;
    }
}
