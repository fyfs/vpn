package kr.co.surveylink.www.mobilelink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * Created by jsyang on 2016-09-06.
 */
public class MCellWifi {

    static private MCellWifi instance;
    static public synchronized MCellWifi getInstance(){
        if(instance==null){
            instance=new MCellWifi();
        }
        return instance;
    }

    //handler
    public void action(String action,int state){
        //Common.log(action);
        //Common.log(state);
        //0 : WIFI_STATE_DISABLING
        //1 : WIFI_STATE_DISABLED
        //2 : WIFI_STATE_ENABLING
        //3 : WIFI_STATE_ENABLED
        //4 : WIFI_STATE_UNKNOWN
    }

    static public class CellWifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.log("CellWifiReceiver onReceive called");
            try {
                String action = intent.getAction();
                WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                MCellWifi.getInstance().action(action,wm.getWifiState());
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
