package kr.co.surveylink.www.mobilelink;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jsyang on 2016-09-02.
 */
public class Common {

    static final String TAG = "MYLOG";
    static final boolean isTest = true;

    //실행 주기
    static int interval_startService = 60000;//서비스가 중지됐는지 확인하는 주기
    static int interval_service = 1000;//서비스 실행 주기
    static int interval_retriveApp = 3000;//Activity 확인 주기

    //최종 실행 시각
    static long lasttime_retriveApp = 0;//최종 Activity 확인시각

    //null to blank
    static String isNull(String str){
        if(str!=null)return str;
        return "";
    }

    //print log
    static void log(String str){if(isTest)Log.d(TAG,str);}
    static void log(int value){if(isTest)Log.d(TAG,Integer.toString(value));}
    static void log(Double value){if(isTest)Log.d(TAG,Double.toString(value));}
    static void log(Long value){if(isTest)Log.d(TAG,Long.toString(value));}
    static void log(HashMap value){if(isTest)Log.d(TAG,value.toString());}
    static void log(boolean value){if(isTest)Log.d(TAG,Boolean.toString(value));}

    //get time
    static String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
