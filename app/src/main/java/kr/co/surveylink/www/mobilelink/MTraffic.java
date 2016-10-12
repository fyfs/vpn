package kr.co.surveylink.www.mobilelink;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jsyang on 2016-09-23.
 */
public class MTraffic implements IDataHandler {

    static private MTraffic instance;
    static public synchronized MTraffic getInstance(){
        if(instance==null){
            instance=new MTraffic();
        }
        return instance;
    }

    /** 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠 */
    private JSONArray data = new JSONArray();


    /**
     * 특정 package 데이터 전송량 반환
     * @param packageName packageName
     * @return 전송량
     */
    public HashMap<String,Long> getRxTx(String packageName){
        HashMap<String,Long> rxtx = new HashMap<>();
        rxtx.put("rx",0l);
        rxtx.put("tx",0l);
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if(!process.getPackageName().equals(packageName))continue;
                    int uid=process.uid;
                    rxtx.put("tx",TrafficStats.getUidTxBytes(uid));
                    rxtx.put("rx",TrafficStats.getUidRxBytes(uid));
                    break;
                } catch (Exception e) {

                }
            }
        }
        return rxtx;
    }

    /**
     * 전체 프로세스의 데이터 전송량 확인
     */
    public void checkTraffic(){
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    int uid=process.uid;
                    long send = TrafficStats.getUidRxBytes(uid);
                    long receive = TrafficStats.getUidTxBytes(uid);
                    Common.log(process.getPackageName()+":"+Long.toString(send)+"/"+Long.toString(receive));
                } catch (Exception e) {

                }
            }
        }
        /*
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
        if (runningProcesses != null && runningProcesses.size() > 0) {
            for(int i=0;i<runningProcesses.size();i++){
                ActivityManager.RunningAppProcessInfo appInfo = runningProcesses.get(i);
                int uid=appInfo.uid;
                long send = TrafficStats.getUidRxBytes(uid);
                long receive = TrafficStats.getUidTxBytes(uid);
                String[] pkgs = appInfo.pkgList;
                Common.log("---"+Integer.toString(uid));
                for(int j=0;j<pkgs.length;j++){
                    Common.log(pkgs[j]+":"+Long.toString(send)+"/"+Long.toString(receive));
                }
            }
        } else {
            Toast.makeText(context, "No application is running", Toast.LENGTH_LONG).show();
        }
        */
    }

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        if(!MPermissions.getInstance().isPermissionOk(context))return;
        if(Common.getInstance().getPreference(context,"deviceId").equals(""))return;
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_TRAFFIC_SAVE, context.getString(R.string.url_MTraffic), params, this);
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_TRAFFIC_SAVE:
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
