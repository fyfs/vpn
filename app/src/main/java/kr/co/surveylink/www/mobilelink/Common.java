package kr.co.surveylink.www.mobilelink;

import android.util.Log;

/**
 * Created by jsyang on 2016-09-02.
 */
public class Common {

    static final String TAG = "MYLOG";
    static final boolean isTest = true;

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
}
