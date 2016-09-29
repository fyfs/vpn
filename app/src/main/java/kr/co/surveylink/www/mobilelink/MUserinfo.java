package kr.co.surveylink.www.mobilelink;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jsyang on 2016-09-28.
 */
public class MUserinfo implements IDataHandler {

    static private MUserinfo instance;
    static public synchronized MUserinfo getInstance(){
        if(instance==null){
            instance=new MUserinfo();
        }
        return instance;
    }

    /** 여러건을 모아서 한 번에 저장하기 위해 data 에 담아둠 */
    private JSONArray data = new JSONArray();
    private int save_cnt=0;

    /**
     * data 에 저장되어 있는 값들을 전송함
     * @param context context
     */
    public void save(Context context){
        String currentTime = Long.toString(new Date().getTime());
        Object[][] params = {{"list",data.toString()},{"currentTime",currentTime}};
        if(Common.getInstance().loadData(Common.HttpAsyncTask.CALLTYPE_USERINFO, context.getString(R.string.url_MUserinfo), params, this))save_cnt++;
    }

    /**
     * 전송 후 종류에 따른 분기
     * @param calltype 전송의 종류
     * @param str 결과 문자열
     */
    public void dataHandler(int calltype, String str){
        switch(calltype){
            case Common.HttpAsyncTask.CALLTYPE_USERINFO:
                saveHandler(str);
                break;
        }
    }

    /**
     * 저장된 값의 전송 결과 처리
     * @param result 결과 문자열
     */
    private void saveHandler(String result) {
        save_cnt--;
        try {
            JSONObject json = new JSONObject(result);
            String err = json.getString("ERR");
            if(err.equals("") && save_cnt==0){
                data = new JSONArray();
            }
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * push token이 변경되었을 때 저장
     * @param context context
     */
    public void savePushToken(Context context){
        if(Common.getInstance().isNull(Common.getPreference(context,"deviceId")).equals(""))return;
        String token = Common.getInstance().isNull(FirebaseInstanceId.getInstance().getToken());
        if(token.equals(""))return;
        if(Common.getPreference(context,"token").equals(token))return;
        Common.setPreference(context,"token",token);
        try {
            JSONObject json = new JSONObject();
            String currentTime = Long.toString(new Date().getTime());
            json.put("K","token");
            json.put("V",token);
            json.put("T",currentTime);
            data.put(json);
            save(context);
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * 권한 저장
     * @param context context
     * @param permission 권한 상태
     */
    public void permissionSave(Context context,int permission){
        try {
            JSONObject json = new JSONObject();
            String currentTime = Long.toString(new Date().getTime());
            json.put("K","permission");
            json.put("V",Integer.toString(permission));
            json.put("T",currentTime);
            data.put(json);
            save(context);
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

}
