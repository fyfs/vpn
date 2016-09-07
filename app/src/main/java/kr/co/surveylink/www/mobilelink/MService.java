package kr.co.surveylink.www.mobilelink;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsyang on 2016-09-05.
 */
public class MService extends Service {

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
                        Common.log("test");
                        SystemClock.sleep(5000);
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
