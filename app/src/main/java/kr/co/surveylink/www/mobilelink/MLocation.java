package kr.co.surveylink.www.mobilelink;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jsyang on 2016-09-02.
 */
public class MLocation {

    static private MLocation instance;
    static public synchronized MLocation getInstance(){
        if(instance==null){
            instance=new MLocation();
        }
        return instance;
    }

    //CellId 방식으로 가져오기
    public HashMap<String,String> getByCellId(Context context){
        Common.log("MLocation getByCellId called");
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        HashMap<String,String> ret = new HashMap<String,String>();
        ret.put("CellLocation",cellLocation.toString());
        ret.put("GSM CELL ID",Integer.toString(cellLocation.getCid()));
        ret.put("GSM MLocation Code",Integer.toString(cellLocation.getLac()));
        if(Common.isTest)Log.d(Common.TAG,ret.toString());
        return ret;
    }

    //GPS 방식으로 가져오기
    public HashMap<String,String> getGps(Context context){
        Common.log("MLocation getGps called");
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        HashMap<String,String> ret = new HashMap<String,String>();
        ret.put("lat",null);
        ret.put("lon",null);
        ret.put("acr",null);
        try {
            Location location = locationManager.getLastKnownLocation(bestProvider);
            ret.put("lat",Double.toString(location.getLatitude()));//37.5608695
            ret.put("lon",Double.toString(location.getLongitude()));//126.9619209
            ret.put("acr",Double.toString(location.getAccuracy()));//30.0
        }
        catch (SecurityException | NullPointerException e){
            //permission 허용안함
            e.printStackTrace();
        }
        Common.log(ret.toString());
        return ret;
    }

}
