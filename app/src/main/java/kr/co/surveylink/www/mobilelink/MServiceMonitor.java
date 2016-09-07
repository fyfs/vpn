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
import android.os.IBinder;
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

    public void startMonitoring(Context context) {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent = new Intent(context, MonitorBR.class);
        sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), Common.interval_startService, sender);
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

        @Override
        public void onCreate() {
            super.onCreate();
            mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            serviceRunning = true;

            //앱 설치/제거/업데이트 로그
            //MAddRemove.getInstance().addReceiver(getApplicationContext());
        }

        @Override
        public void onDestroy() {
            serviceRunning = false;
            //MAddRemove.getInstance().removeReceiver(getApplicationContext());
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
                            //MActivity retriveApp
                            if(Common.lasttime_retriveApp+Common.interval_retriveApp<now){
                                MActivity.getInstance().retriveApp(getApplicationContext());
                                Common.lasttime_retriveApp=now;
                            }
                            SystemClock.sleep(Common.interval_service);
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