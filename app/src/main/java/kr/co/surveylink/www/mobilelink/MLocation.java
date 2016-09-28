package kr.co.surveylink.www.mobilelink;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 위치정보를 가져오는 class
 */
public class MLocation {

    static private MLocation instance;
    static public synchronized MLocation getInstance(){
        if(instance==null){
            instance=new MLocation();
        }
        return instance;
    }
    
    static final String STR_CELLLOCATION = "CellLocation";
    static final String STR_GSMCELLID = "GSMCELLID";
    static final String STR_GSMLOCATIONCODE = "GSMLocationCode";
    static final String STR_LAT = "lat";
    static final String STR_LON = "lon";
    static final String STR_ACR = "acr";
    static final String STR_ALT = "alt";

    /**
     * CellId 방식으로 가져오기
     * @param context context
     * @return CellLocation,GSMCELLID,GSMMLocationCode
     */
    public HashMap<String,String> getByCellId(Context context){
        //Common.log("MLocation getByCellId called");
        HashMap<String,String> ret = new HashMap<String,String>();
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            Common.log("ACCESS_COARSE_LOCATION permission denied");
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_COARSE_LOCATION,true);
            ret.put(MLocation.STR_CELLLOCATION,"-1");
            ret.put(MLocation.STR_GSMCELLID,"-1");
            ret.put(MLocation.STR_GSMLOCATIONCODE,"-1");
            return ret;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_COARSE_LOCATION,false);
        }
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        ret.put(MLocation.STR_CELLLOCATION,cellLocation.toString());
        ret.put(MLocation.STR_GSMCELLID,Integer.toString(cellLocation.getCid()));
        ret.put(MLocation.STR_GSMLOCATIONCODE,Integer.toString(cellLocation.getLac()));
        return ret;
    }

    /**
     * GPS 방식으로 가져오기
     * @param context context
     * @return lat/lon/acr/alt
     */
    public HashMap<String,String> getGps(Context context){
        //Common.log("MLocation getGps called");
        HashMap<String,String> ret = new HashMap<String,String>();
        ret.put(MLocation.STR_LAT,"-1");
        ret.put(MLocation.STR_LON,"-1");
        ret.put(MLocation.STR_ACR,"-1");
        ret.put(MLocation.STR_ALT,"-1");
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck== PackageManager.PERMISSION_DENIED){
            Common.log("ACCESS_FINE_LOCATION permission denied");
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_FINE_LOCATION,true);
            ret.put(MLocation.STR_LAT,"-2");
            ret.put(MLocation.STR_LON,"-2");
            ret.put(MLocation.STR_ACR,"-2");
            ret.put(MLocation.STR_ALT,"-2");
            return ret;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_ACCESS_FINE_LOCATION,false);
        }
        LocationManager mLocationManager = (LocationManager)context.getApplicationContext().getSystemService(context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(new Criteria(),true);
        Location bestLocation = null;
        if(providers.size()==0) {
            Common.log("위치 서비스가 활성화되어있지 않습니다");
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_GPS,true);
            ret.put(MLocation.STR_LAT,"-3");
            ret.put(MLocation.STR_LON,"-3");
            ret.put(MLocation.STR_ACR,"-3");
            ret.put(MLocation.STR_ALT,"-3");
            return ret;
        } else {
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_GPS,false);
        }
        try {
            for (String provider : providers) {
                Location location = mLocationManager.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                    ret.put(MLocation.STR_LAT,Double.toString(bestLocation.getLatitude()));//37.5608695
                    ret.put(MLocation.STR_LON,Double.toString(bestLocation.getLongitude()));//126.9619209
                    ret.put(MLocation.STR_ACR,Double.toString(bestLocation.getAccuracy()));//30.0
                    ret.put(MLocation.STR_ALT,Double.toString(bestLocation.getAltitude()));//0
                }

            }
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_LOCATION,false);
        }
        catch (SecurityException | NullPointerException e){
            //permission 허용안함
            MPermissions.getInstance().permissionChanged(context,MPermissions.NEED_LOCATION,true);
            ret.put(MLocation.STR_LAT,"-4");
            ret.put(MLocation.STR_LON,"-4");
            ret.put(MLocation.STR_ACR,"-4");
            ret.put(MLocation.STR_ALT,"-4");
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 위치 업데이트를 위해 더미 리스너 생성
     */
    public static LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            try {
                LocationManager mLocationManager = (LocationManager) Common.getInstance().context.getApplicationContext().getSystemService(Common.getInstance().context.LOCATION_SERVICE);
                mLocationManager.removeUpdates(MLocation.locationListener);
            } catch (SecurityException e){
                Common.log(e.toString());
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
