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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jsyang on 2016-09-02.
 */
public class MInstalledApp implements IDataHandler{

    static private MInstalledApp instance;
    static public synchronized MInstalledApp getInstance(){
        if(instance==null){
            instance=new MInstalledApp();
        }
        return instance;
    }

    /**
     * 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠
     */
    private JSONArray data = new JSONArray();

    /**
     * 현재 설치되어있는 앱 정보를 확인해 data 변수에 넣음
     * @param context context
     */
    public void checkInstalledApp(Context context) {
        Common.log("MInstalledApp checkInstalledApp called");
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        data = new JSONArray();
        HashMap<String,String> ret;
        String actionTime = Long.toString(new Date().getTime());
        for (Object obj : pkgAppsList) {
            JSONObject o = new JSONObject();
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

            packageName=Common.getInstance().isNull(packageInfo.packageName);
            if(packageInfo.requestedPermissions !=null){
                for(i=0;i<packageInfo.requestedPermissions.length;i++){
                    permissions+="|"+packageInfo.requestedPermissions[i];
                }
                permissions=permissions.substring(1);
            }
            applicationLabel=Common.getInstance().isNull(context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString());
            firstInstallTime=packageInfo.firstInstallTime;
            lastUpdateTime=packageInfo.lastUpdateTime;
            versionCode=packageInfo.versionCode;
            versionName=packageInfo.versionName;
            try {
                o.put("at", actionTime);
                o.put("pn", packageName);
                o.put("pm", permissions);
                o.put("al", applicationLabel);
                o.put("ft", Long.toString(firstInstallTime));
                o.put("lt", Long.toString(lastUpdateTime));
                o.put("vc", Integer.toString(versionCode));
                o.put("vn", versionName);
            } catch (Exception e){
                e.toString();
            }

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
            data.put(o);
        }
    }

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_INSTALLEDAPP_SAVE, context.getString(R.string.url_MInstalledApp), params, this);
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_INSTALLEDAPP_SAVE:
                saveHandler(str);
                break;
        }
    }

    /**
     * 저장된 값의 전송 결과 처리
     * @param result 결과 문자열
     */
    private void saveHandler(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String err = json.getString("ERR");
            if(err.equals("")){
                data = new JSONArray();
            }
        } catch (Exception e){
            Common.log(e.toString());
        }
    }
}
