package kr.co.surveylink.www.mobilelink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by jsyang on 2016-09-05.
 */
public class MAddRemove {

    static private MAddRemove instance;
    static public synchronized MAddRemove getInstance(){
        if(instance==null){
            instance=new MAddRemove();
        }
        return instance;
    }

    public MAddRemove() {
    }

    //handler
    public void action(String action,String packageName){
        Common.log(action);
        Common.log(packageName);
    }

    static public class AddRemoveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.log("AddRemoveReceiver onReceive called");
            try {
                String action = intent.getAction();
                String packageName = intent.getData().getSchemeSpecificPart();
                MAddRemove.getInstance().action(action, packageName);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
