package kr.co.surveylink.www.mobilelink;

import android.Manifest;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.List;

/**
 * 권한 처리 class
 */
public class MPermissions{

    static private MPermissions instance;
    static public synchronized MPermissions getInstance(){
        if(instance==null){
            instance=new MPermissions();
        }
        return instance;
    }

    /**
     * 권한 요청
     * @param activity activity
     */
    void requestPermissions(Activity activity){
        ActivityCompat.requestPermissions(activity,new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
                ,Manifest.permission.ACCESS_COARSE_LOCATION
                ,Manifest.permission.ACCESS_NETWORK_STATE
                ,Manifest.permission.READ_PHONE_STATE
        },0);
    }


    /**
     * 권한 전체를 확인해 권한 요청이 필요하면 true를 반환
     * @param context context
     * @return 권한요청 필요여부
     */
    boolean needPermissions(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED)return true;
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED)return true;
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)== PackageManager.PERMISSION_DENIED)return true;
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_DENIED)return true;
        return false;
    }

    /**
     * 사용정보 접근 허용여부를 확인해 요청이 필요하면 true를 반환
     * @param context context
     * @return 사용정보 접근 요청 필요여부
     */
    boolean needUsageAccess(Context context){
        if (Build.VERSION.SDK_INT >= 21) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, time - 1000 * 1000, time);
            if(applist.size()==0)return true;
        }
        return false;
    }

    /**
     * 전체 권한이 허용된 상태인지 확인
     * 이 값이 false면 저장을 하지 않는다
     * @param context context
     * @return 허용여부
     */
    boolean isPermissionOk(Context context){
        boolean isOk = true;
        if(needPermissions(context))isOk=false;
        if(needUsageAccess(context))isOk=false;
        return isOk;
    }

}
