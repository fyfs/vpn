package kr.co.surveylink.www.mobilelink;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Display;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 앱의 실행여부와 시간을 확인하는 class
 */
public class MActivity implements IDataHandler {

    static private MActivity instance;
    static public synchronized MActivity getInstance(){
        if(instance==null){
            instance=new MActivity();
        }
        return instance;
    }

    /** 마지막으로 실행한 packageName */
    private String lastPackageName = "";
    /** 마지막으로 실행한 activity */
    private String lastActivity = "";
    /** package가 여러개 조회되었을 때엔 기존 package들에서 추가된 것을 확인해서 판별하기 위해 기존 리스트를 저장해둔다 */
    private List<String> lastPackages = new ArrayList<>();
    /** 앱별 시작시각 */
    HashMap<String,Long> appStartTime = new HashMap<>();
    /** 앱 데이터 수신량 */
    HashMap<String,Long> appStartRx = new HashMap<>();
    /** 앱 데이터 송신량 */
    HashMap<String,Long> appStartTx = new HashMap<>();

    /** 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠 */
    private JSONArray data = new JSONArray();

    public void retriveApp(Context context) {
        String packageName = "";
        String processName = "";
        String activity = "";
        List<String> curPackages = new ArrayList<>();
        boolean displayOn = isDisplayOn(context);
        if(displayOn) {
            try {
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                processName = am.getRunningAppProcesses().get(0).processName;
                if (Build.VERSION.SDK_INT >= 21) {
                    // 설정 - 보안에서 사용정보 허용앱 권한을 허용하면 아래 방법으로 가져올 수 있다
                    UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                    long time = System.currentTimeMillis();
                    List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, time - 1000 * 1000, time);
                    if (applist != null && applist.size() > 0) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                        for (UsageStats usageStats : applist) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (mySortedMap != null && !mySortedMap.isEmpty()) {
                            packageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                    }

                    if(packageName.equals("")) {
                        List<AndroidAppProcess> ap = AndroidProcesses.getRunningForegroundApps(context);
                        if (ap.size()>1){
                            //기존 리스트가 없으면 가장 뒤의 것을 가져온다
                            if(lastPackages.size()==0) {
                                packageName=ap.get(ap.size()-1).getPackageName();
                            } else {
                                //기존 리스트가 있으면 기존 리스트와 겹치는 것을 제외시킨다
                                for(AndroidAppProcess aap : ap){
                                    curPackages.add(aap.getPackageName());
                                }
                                List<String> resultPackages = removeDupFromOld(lastPackages,curPackages);
                                //결과리스트가 하나도 없으면 그냥 맨 위의 것으로 반환
                                if(resultPackages.size()==0){
                                    packageName=ap.get(ap.size()-1).getPackageName();
                                } else {
                                    //결과리스트가 있으면 결과 중 가장 상위 것으로 반환
                                    packageName=resultPackages.get(resultPackages.size()-1);
                                }
                            }
                        } else if (ap.size() > 0) {
                            packageName=ap.get(0).getPackageName();
                        }
                    }
                    /*
                    Common.log("1:"+packageName);
                    Common.log("2:"+am.getRunningTasks(1).get(0).topActivity.getPackageName());
                    Common.log("3:"+processName);
                    Common.log("4:"+getActivePackagesCompat(am));
                    String[] activePackages = getActivePackages(am);
                    packageName=guessPackageName(activePackages,processName);
                    Common.log("5:"+packageName);
                    Common.log("6:"+(am.getRunningTasks(1).get(0)).topActivity.getPackageName());
                    Common.log("7:"+(am.getRunningTasks(1).get(0)).topActivity.getClassName());
                    */
                } else {
                    packageName = (am.getRunningTasks(1).get(0)).topActivity.getPackageName();
                    activity = (am.getRunningTasks(1).get(0)).topActivity.getClassName();
                }
            } catch (Exception e){
                Common.log(e.toString());
            }
        }
        if(!lastPackageName.equals(packageName)){
            change(context);
            save(context);
            lastPackageName=packageName;
            lastActivity=activity;
            lastPackages=curPackages;
            appStartTime.put(packageName,new Date().getTime());
            HashMap<String,Long> rxtx = MTraffic.getInstance().getRxTx(packageName);
            appStartRx.put(packageName,rxtx.get("rx"));
            appStartTx.put(packageName,rxtx.get("tx"));
        }
    }

    /**
     * 새로운 리스트에서 기존 리스트와 겹치는 것을 제거한다
     * @param old 기존 리스트
     * @param now 새로운 리스트
     * @return 결과 리스트
     */
    private List<String> removeDupFromOld(List<String> old,List<String> now){
        List<String> ret = new ArrayList<>();
        for(String nowStr : now){
            boolean isExists = false;
            for(String oldStr : old){
                if(nowStr.equals(oldStr))isExists=true;
            }
            if(!isExists)ret.add(nowStr);
        }
        return ret;
    }

    /**
     * 실행중인 package 가져오기
     * @param mActivityManager ActivityManager
     * @return 실행중인 package
     */
    String getActivePackagesCompat(ActivityManager mActivityManager) {
        final List<ActivityManager.RunningTaskInfo> taskInfo = mActivityManager.getRunningTasks(1);
        final ComponentName componentName = taskInfo.get(0).topActivity;
        return componentName.getPackageName();
    }

    /**
     * 실행중인 package들 가져오기
     * @param mActivityManager ActivityManager
     * @return 실행중인 package들
     */
    String[] getActivePackages(ActivityManager mActivityManager) {
        final Set<String> activePackages = new HashSet<String>();
        final List<ActivityManager.RunningAppProcessInfo> processInfos = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                activePackages.addAll(Arrays.asList(processInfo.pkgList));
            }
        }
        return activePackages.toArray(new String[activePackages.size()]);
    }

    /**
     * packageName을 가져올 수 없는 경우 processName과 가장 비슷한 것으로 추정
     */
    private String guessPackageName(String[] packageNames,String processName){
        String ret = "";
        int maxSameLength=0;
        int i;
        for(String packageName : packageNames){
            for(i=0;i<processName.length();i++){
                if(!packageName.substring(0,i+1).equals(processName.substring(0,i+1)))break;
            }
            if(i>maxSameLength){
                maxSameLength=i;
                ret=packageName;
            }
        }
        if(packageNames.equals("")){
            ret=processName;
            Common.log("Oops");
        }
        return ret;
    }

    /**
     * 화면이 켜져있는지 확인
     * @param context context
     * @return true / false
     */
    private boolean isDisplayOn(Context context){
        if(Build.VERSION.SDK_INT>=20) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState()==Display.STATE_ON) {
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

    /**
     * foreground 앱이 변경된 경우 실행됨
     */
    private void change(Context context){
        //처음엔 공백이므로 저장하지 않음
        if(lastPackageName.equals(""))return;
        //사용시간
        Long startTime = appStartTime.get(lastPackageName);
        Long useTime = (new Date().getTime())-startTime;
        //사용시간이 너무 짧으면 저장하지 않음
        if(useTime<1000)return;
        //데이터 사용량
        Long startRx = appStartRx.get(lastPackageName);
        Long startTx = appStartTx.get(lastPackageName);
        HashMap<String,Long> rxtx = MTraffic.getInstance().getRxTx(lastPackageName);
        Long useRx = rxtx.get("rx")-startRx;
        Long useTx = rxtx.get("tx")-startTx;
        if(useRx<0)useRx=-1l;
        if(useTx<0)useTx=-1l;

        JSONObject obj = new JSONObject();
        String cellWifi = "";
        try {
            cellWifi = MCellWifi.getInstance().getNetworkState(context);
            HashMap<String,String> cellId = MLocation.getInstance().getByCellId(context);
            HashMap<String,String> gps = MLocation.getInstance().getGps(context);
            obj.put("at",Long.toString(new Date().getTime()));
            obj.put("pn", lastPackageName);
            obj.put("av", lastActivity);
            obj.put("st", Long.toString(startTime));
            obj.put("ut", Long.toString(useTime));
            obj.put("cw", cellWifi);
            obj.put("cl", cellId.get(MLocation.STR_CELLLOCATION));
            obj.put("gi", cellId.get(MLocation.STR_GSMCELLID));
            obj.put("gl", cellId.get(MLocation.STR_GSMLOCATIONCODE));
            obj.put("lt", gps.get(MLocation.STR_LAT));
            obj.put("ln", gps.get(MLocation.STR_LON));
            obj.put("ac", gps.get(MLocation.STR_ACR));
            obj.put("al", gps.get(MLocation.STR_ALT));
            obj.put("rx", Long.toString(useRx));
            obj.put("tx", Long.toString(useTx));
            //통계정보 저장
            int appFreq = Common.getInstance().isNull(MStatistics.getInstance().appFreq.get(lastPackageName));
            MStatistics.getInstance().appFreq.put(lastPackageName,++appFreq);
            Long appLong = Common.getInstance().isNull(MStatistics.getInstance().appLong.get(lastPackageName));
            MStatistics.getInstance().appLong.put(lastPackageName,appLong+useTime);
            MStatistics.getInstance().appUseTime+=useTime;
        } catch (Exception e){
            Common.log(e.toString());
        }
        data.put(obj);
        Common.log(obj.toString());
        if(Common.getInstance().sendImmediately || cellWifi.equals("WIFI"))save(context);
    }

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        if(!MPermissions.getInstance().isPermissionOk(context))return;
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_ACTIVITY_SAVE, context.getString(R.string.url_MActivity), params, this);
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_ACTIVITY_SAVE:
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
                //정상적으로 저장된 경우 data 비움
                data = new JSONArray();
            }
        } catch (Exception e){

        }
    }

}
