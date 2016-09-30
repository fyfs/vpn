package kr.co.surveylink.www.mobilelink;

/**
 * Created by jsyang on 2016-09-05.
 */
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.VpnService;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import java.util.Date;
import java.util.List;

public class MServiceMonitor {

    private static MServiceMonitor instance;
    private AlarmManager am;
    private Intent intent;
    private PendingIntent sender;

    private MServiceMonitor() {}
    public static synchronized MServiceMonitor getInstance() {
        if (instance == null) {
            instance = new MServiceMonitor();
        }
        return instance;
    }

    public static class MonitorBR extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRunningService(context, MService.class) == false) {
                context.startService(new Intent(context, MService.class));
            }
        }
    }

    /**
     * 재부팅 되었을 때 서비스를 재가동하기 위해 Receive
     */
    public static class BootCompletedIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                context.startService(new Intent(context, MyAccessibilityService.class));
                context.startService(new Intent(context, MService.class));
                context.startService(new Intent(context, ToyVpnService.class));
                /* reboot 후에 위치를 가져오는데 오래걸려서 listener를 등록해 바로 가져오도록 한다 */
                try {
                    LocationManager mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MLocation.locationListener, Looper.getMainLooper());
                } catch (SecurityException e){
                    Common.log(e.toString());
                }
            }
        }
    }

    public void startVpn(){
        try {
            Thread.sleep(1000);
            Common.log("startVpn");
            Intent vIntent = VpnService.prepare(Common.getInstance().context);
            Common.getInstance().context.startService(new Intent(Common.getInstance().context, ToyVpnService.class));
        } catch(Exception e){
            Common.log(e.toString());
        }
    }

    public void startMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, MonitorBR.class);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), Common.getInstance().interval_startService, sender);
    }

    public void stopMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, MonitorBR.class);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(sender);
        am = null;
        sender = null;
    }

    public boolean isMonitoring() {
        return (MService.mThread == null || MService.mThread.isAlive() == false) ? false : true;
    }

    private static boolean isRunningService(Context context, Class<?> cls) {
        boolean isRunning = false;

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (info != null) {
            for(ActivityManager.RunningServiceInfo serviceInfo : info) {
                ComponentName compName = serviceInfo.service;
                String className = compName.getClassName();
                if(className.equals(cls.getName())) {
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }

    static public class MService extends Service {

        public static Thread mThread;
        private boolean serviceRunning = false;
        private ComponentName recentComponentName;
        private ActivityManager mActivityManager;
        private Service mService;

        @Override
        public void onCreate() {
            mService=this;
            super.onCreate();
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            serviceRunning = true;
        }

        @Override
        public void onDestroy() {
            serviceRunning = false;
            super.onDestroy();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId){
            if (mThread == null) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (serviceRunning) {
                            Long now = (new Date()).getTime();
                            Common.getInstance().context=getApplicationContext();
                            //MActivity retriveApp
                            if(Common.getInstance().lasttime_retriveApp+Common.getInstance().interval_retriveApp<now){
                                MActivity.getInstance().retriveApp(getApplicationContext());
                                Common.getInstance().lasttime_retriveApp=now;
                            }
                            //Permission need notification
                            if(MPermissions.getInstance().currentPermission>0) {
                                if (Common.getInstance().lasttime_noti_permission + Common.getInstance().interval_noti_permission < now) {
                                    MPermissions.getInstance().showPermissionNotification(mService,getApplicationContext());
                                    Common.getInstance().lasttime_noti_permission = now;
                                }
                            }
                            SystemClock.sleep(Common.getInstance().interval_service);
                        }
                    }
                });
                mThread.start();
            } else if (mThread.isAlive() == false) {
                mThread.start();
            }
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent){
            return null;
        }
    }
}