package kr.co.surveylink.www.mobilelink;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

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

    static public final int NEED_ACCESS_FINE_LOCATION = 0;//원래 1인데 필수권한 아니어서 빠짐
    static public final int NEED_ACCESS_COARSE_LOCATION = 0;//원래 2인데 필수권한 아니어서 빠짐
    static public final int NEED_ACCESS_NETWORK_STATE = 4;
    static public final int NEED_READ_PHONE_STATE = 8;
    static public final int NEED_USAGE_STATS_SERVICE = 16;
    static public final int NEED_FIREBASE_CLOUD_MESSAGE = 32;
    static public final int NEED_GPS= 64;
    static public final int NEED_LOCATION= 128;
    static public final int NEED_VPN= 256;

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
        boolean need = false;
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_DENIED){
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_READ_PHONE_STATE,true);
            need=true;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_READ_PHONE_STATE,false);
            //push token 저장
            MUserinfo.getInstance().savePushToken(context);
        }
        /*
        //위치 권한은 필수가 아닌 걸로
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_FINE_LOCATION,true);
            need=true;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_FINE_LOCATION,false);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED){
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_COARSE_LOCATION,true);
            need=true;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_COARSE_LOCATION,false);
        }
        */
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)== PackageManager.PERMISSION_DENIED){
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_NETWORK_STATE,true);
            need=true;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_NETWORK_STATE,false);
        }
        return need;
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
            if(applist.size()==0){
                MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_USAGE_STATS_SERVICE,true);
                return true;
            } else {
                MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_USAGE_STATS_SERVICE,false);
            }
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

    /** 현재 권한 */
    public int currentPermission = 0;
    /**
     * 권한이 필요함
     * @param context context
     * @param permission MPermissions.NEED_xxx 로 정의되어 있음
     * @param isAdd true : 권한이 필요함 false : 권한 허용완료
     */
    public void permissionChanged(Context context,int permission,boolean isAdd) {
        boolean mustChange = false;
        if (isAdd) {
            //허용되지 않은 권한이 있는 경우
            if ((currentPermission | permission) != currentPermission) mustChange = true;
            currentPermission = (currentPermission | permission);
        } else {
            //필요한 권한이 허용된 경우
            if (((currentPermission | permission) - permission) != currentPermission)
                mustChange = true;
            currentPermission = (currentPermission | permission)-permission;
        }
        Common.setPreference(context,"currentPermission",Integer.toString(currentPermission));
        if (!mustChange) return;
        MUserinfo.getInstance().permissionSave(context,currentPermission);
    }

    public void showPermissionNotification(Service s, Context context){
        //현재 실행중이면 푸시하지 않는다
        if(MActivity.getInstance().lastPackageName.equals(context.getPackageName()))return;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.common_plus_signin_btn_text_light).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.noti_needPermission))
                .setAutoCancel(true)
                .setSound(defaultSoundUri).setLights(000000255, 500, 2000)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakelock.acquire(5000);

    }

}
