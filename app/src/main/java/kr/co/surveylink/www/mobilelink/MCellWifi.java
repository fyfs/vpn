package kr.co.surveylink.www.mobilelink;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Cellular 또는 wifi의 상태를 확인하는 class
 */
public class MCellWifi {

    static private MCellWifi instance;
    static public synchronized MCellWifi getInstance(){
        if(instance==null){
            instance=new MCellWifi();
        }
        return instance;
    }

    /**
     * Wifi의 상태가 변경되었을 때 실행됨
     * @param action wifi 상태변경 action
     * @param state wifi 상태 ( 0/1/2/3/4 )
     */
    public void action(String action,int state){
        //Common.log(action);
        //Common.log(state);
        //0 : WIFI_STATE_DISABLING
        //1 : WIFI_STATE_DISABLED
        //2 : WIFI_STATE_ENABLING
        //3 : WIFI_STATE_ENABLED
        //4 : WIFI_STATE_UNKNOWN
    }
    /**
     * wifi 상태가 변경되었을 때 수신자 - 현재 사용하지 않음
     */
    static public class CellWifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
            Common.log("CellWifiReceiver onReceive called");
            try {
                String action = intent.getAction();
                WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                MCellWifi.getInstance().action(action,wm.getWifiState());
            } catch(Exception e){
                e.printStackTrace();
            }
            */
        }
    }

    /**
     * 현재 data 연결상태를 반환함
     * @param context context
     * @return none / WIFI / MOBILE
     */
    public String getNetworkState(Context context){
        String state = "none";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                state=activeNetwork.getTypeName();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                state=activeNetwork.getTypeName();
            }
        }
        return state;
    }

}
