package kr.co.surveylink.www.mobilelink;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by jsyang on 2016-09-07.
 */
public class MActivity {

    static private MActivity instance;
    static public synchronized MActivity getInstance(){
        if(instance==null){
            instance=new MActivity();
        }
        return instance;
    }

    /*
    public void action(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService (Context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
        {

            String packageName = activityManager.getRunningAppProcesses().get(0).processName;
        }
        else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            String packageName =  ProcessManager.getRunningForegroundApps(getApplicationContext()).get(0).getPackageName();

        }
        else
        {
            String packageName = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }

    }

    public static String getTopAppName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strName = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                strName = getLollipopFGAppPackageName(context);
            } else {
                strName = mActivityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strName;
    }


    private static String getLollipopFGAppPackageName(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService("usagestats");
                long milliSecs = 60 * 1000;
                Date date = new Date();
                List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, date.getTime() - milliSecs, date.getTime());
                if (queryUsageStats.size() > 0) {
                    Log.i("LPU", "queryUsageStats size: " + queryUsageStats.size());
                }
                long recentTime = 0;
                String recentPkg = "";
                for (int i = 0; i < queryUsageStats.size(); i++) {
                    UsageStats stats = queryUsageStats.get(i);
                    if (i == 0 && !"org.pervacio.pvadiag".equals(stats.getPackageName())) {
                        Log.i("LPU", "PackageName: " + stats.getPackageName() + " " + stats.getLastTimeStamp());
                    }
                    if (stats.getLastTimeStamp() > recentTime) {
                        recentTime = stats.getLastTimeStamp();
                        recentPkg = stats.getPackageName();
                    }
                }
                return recentPkg;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
*/
    private String lastPackageName = "";
    private String lastActivity = "";
    private long startTime = 0;
    private long useTime = 0;
    private List<HashMap<String,String>> list = new ArrayList<>();
    public void retriveApp(Context context) {
        String packageName = "";
        String activity = "";
        if(isDisplayOn(context)) {
            if (Build.VERSION.SDK_INT >= 21) {
                UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
                if (applist != null && applist.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                    for (UsageStats usageStats : applist) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        packageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
            } else {
                ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                packageName = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
                activity = (manager.getRunningTasks(1).get(0)).topActivity.getClassName();
            }
        }
        if(lastPackageName.equals(packageName)){
            useTime=(new Date()).getTime()-startTime;
        } else {
            change();
            lastPackageName=packageName;
            lastActivity=activity;
            startTime=(new Date()).getTime();
        }
    }

    //화면이 켜져있는지 확인
    private boolean isDisplayOn(Context context){
        if(Build.VERSION.SDK_INT>=20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    return true;
                }
            }
            return false;
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(context.POWER_SERVICE);
            if (powerManager.isScreenOn()) {
                return true;
            }
            return false;
        }
    }

    //foreground 앱이 변경된 경우
    private void change(){
        //처음엔 공백이므로 저장하지 않음
        if(lastPackageName.equals(""))return;
        HashMap<String,String> data = new HashMap<>();
        data.put("packageName",lastPackageName);
        data.put("activity",lastActivity);
        data.put("startTime",Long.toString(startTime));
        data.put("useTime",Long.toString(useTime));
        list.add(data);
        Common.log(data);
    }

}
