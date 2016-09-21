package kr.co.surveylink.www.mobilelink;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 통계자료를 화면에 보여주기 위한 class
 */
public class MStatistics {

    static private MStatistics instance;
    static public synchronized MStatistics getInstance(){
        if(instance==null){
            instance=new MStatistics();
        }
        return instance;
    }

    public HashMap<String,String> appNames = new HashMap<>();
    public HashMap<String,Integer> appFreq = new HashMap<>();
    public HashMap<String,Long> appLong = new HashMap<>();

    private Long statStartTime = new Date().getTime();
    public Long appUseTime = 0l;

    /**
     * 통계정보 초기화
     */
    public void reset(){
        appFreq = new HashMap<>();
        appLong = new HashMap<>();
        appUseTime = 0l;
        statStartTime = new Date().getTime();
    }

    /**
     * 앱 리스트에 없는 사용정보 제거
     */
    public void cleanStat(){
        Set<Map.Entry<String,Integer>> setAppFreq = appFreq.entrySet();
        Set<Map.Entry<String,Long>> setAppLong = appLong.entrySet();
        Set<Map.Entry<String,String>> setAppNames = appNames.entrySet();
        List<String> appFreqDelete = new ArrayList<>();
        List<String> appLongDelete = new ArrayList<>();
        boolean exists;
        for(Map.Entry<String,Integer> appFreq : setAppFreq){
            exists=false;
            for(Map.Entry<String,String> appNames : setAppNames){
                if(appFreq.getKey().equals(appNames.getKey()))exists=true;
            }
            if(!exists) appFreqDelete.add(appFreq.getKey());
        }
        for(String key : appFreqDelete)appFreq.remove(key);
        for(Map.Entry<String,Long> appLong : setAppLong){
            exists=false;
            for(Map.Entry<String,String> appNames : setAppNames){
                if(appLong.getKey().equals(appNames.getKey()))exists=true;
            }
            if(!exists) appLongDelete.add(appLong.getKey());
        }
        for(String key : appLongDelete)appLong.remove(key);
    }

    /**
     * 가장 자주 실행한 앱 표시
     * @return 앱 이름
     */
    public String getFreqApp(Context context){
        Set<Map.Entry<String, Integer>> setAppFreq = appFreq.entrySet();
        int maxFreq = -1;
        String packageName = "";
        for (Map.Entry<String, Integer> appFreq : setAppFreq) {
            if (appFreq.getValue() > maxFreq) {
                maxFreq = appFreq.getValue();
                packageName = appFreq.getKey();
            }
        }
        if(packageName.equals("")){
            return context.getString(R.string.lbl_noStat);
        }
        return appNames.get(packageName);
    }

    /**
     * 가장 오래 실행한 앱 표시
     * @return 앱 이름
     */
    public String getLongApp(Context context){
        Set<Map.Entry<String, Long>> setAppLong = appLong.entrySet();
        Long maxLong = -1l;
        String packageName = "";
        for (Map.Entry<String, Long> appLong : setAppLong) {
            if (appLong.getValue() > maxLong) {
                maxLong = appLong.getValue();
                packageName = appLong.getKey();
            }
        }
        if(packageName.equals("")){
            return context.getString(R.string.lbl_noStat);
        }
        return appNames.get(packageName);
    }

    /**
     * 장치 사용시간 표시
     * @return 앱 이름
     */
    public String getUseTime(Context context){
        String ret = "";
        Long currentTime = new Date().getTime();
        Long totalTime = currentTime-statStartTime;
        ret+=Common.getInstance().timeToStr(appUseTime)+" / ";
        ret+=Common.getInstance().timeToStr(totalTime);
        return ret;
    }

}
