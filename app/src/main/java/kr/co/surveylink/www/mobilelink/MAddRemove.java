package kr.co.surveylink.www.mobilelink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * 앱 설치/제거/업데이트 로그를 남기기 위한 class
 */
public class MAddRemove implements IDataHandler {

    static private MAddRemove instance;
    static public synchronized MAddRemove getInstance(){
        if(instance==null){
            instance=new MAddRemove();
        }
        return instance;
    }

    /** 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠 */
    private JSONArray data = new JSONArray();

    /**
     * 설치/삭제/업데이트가 발생했을 때 실행됨
     * @param action 발생한 이벤트 (PACKAGE_ADDED / PACKAGE_FULLY_REMOVED / PACKAGE_REPLACED)
     * @param packageName 패키지명 (kr.co.marketlink.ideapanel)
     */
    public void action(Context context, String action,String packageName){
        JSONObject obj = new JSONObject();
        try{
            obj.put("at",Long.toString(new Date().getTime()));
            obj.put("act",action.replace("android.intent.action.",""));
            obj.put("pkg",packageName);
            data.put(obj);
            String cellWifi = MCellWifi.getInstance().getNetworkState(context);
            if(Common.getInstance().sendImmediately || cellWifi.equals("WIFI"))save(context);
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        if(!MPermissions.getInstance().isPermissionOk(context))return;
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        Common.getInstance().lastsave_addRemove=new Date().getTime();
        Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_ADDREMOVE_SAVE, context.getString(R.string.url_MAddRemove), params, this);
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_ADDREMOVE_SAVE:
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

    /**
     * 앱 설치/삭제/업데이트가 발생할 경우 수신자
     */
    static public class AddRemoveReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Common.log("AddRemoveReceiver onReceive called");
            try {
                String action = intent.getAction();
                String packageName = intent.getData().getSchemeSpecificPart();
                MAddRemove.getInstance().action(context, action, packageName);
                if(Common.getInstance().sendImmediately)MAddRemove.getInstance().save(context);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
