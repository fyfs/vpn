package kr.co.surveylink.www.mobilelink;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * 일반적으로 사용되는 것들을 모아놓은 class
 */
public class Common {

    static private Common instance;
    static public synchronized Common getInstance(){
        if(instance==null){
            instance=new Common();
        }
        return instance;
    }

    static private String preferenceName = "userinfo";
    static private String secureKey = "app_mobileLink";

    /** Log 남길 때 필터를 걸기 위한 TAG */
    final String TAG = "MYLOG";
    /** 테스트중인지 여부 */
    final boolean isTest = true;
    /** 모든 전송을 바로 할 것인지 여부 */
    boolean sendImmediately = false;
    /** data 전송시 처리되야 할 activity 작업이 있다면 사용 */
    Activity activity;
    /** data 전송시 처리되야 할 context 작업이 있다면 사용 */
    Context context;
    /** data 전송시 결과처리 핸들러를 지정 */
    IDataHandler dataHandler;
    /** 서비스가 중지됐는지 확인하는 주기 */
    int interval_startService = 60000;
    /** 권한 허용되지 않았을 때 noti를 보여주는 주기 */
    int interval_noti_permission = 1000*60*60;
    /** 서비스 실행 주기 */
    int interval_service = 1000;
    /** Activity 확인 주기 */
    int interval_retriveApp = 3000;
    /** 최종 Activity 확인시각 */
    long lasttime_retriveApp = 0;
    /** 최종 권한 허용 요청 noti 시각 */
    long lasttime_noti_permission = 0;

    /**
     * null 을 공백으로 반환
     * @param str 입력 문자
     * @return 반환값
     */
    String isNull(String str){
        if(str!=null)return str;
        return "";
    }

    /**
     * null 을 0으로 반한
     * @param num 입력값
     * @return 반환값
     */
    Integer isNull(Integer num){
        if(num!=null)return num;
        return 0;
    }

    /**
     * null 을 0으로 반한
     * @param num 입력값
     * @return 반환값
     */
    Long isNull(Long num){
        if(num!=null)return num;
        return 0l;
    }

    /** 로그 출력 */
    static void log(String str){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,str);}
    /** 로그 출력 */
    static void log(int value){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,Integer.toString(value));}
    /** 로그 출력 */
    static void log(Double value){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,Double.toString(value));}
    /** 로그 출력 */
    static void log(Long value){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,Long.toString(value));}
    /** 로그 출력 */
    static void log(HashMap value){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,value.toString());}
    /** 로그 출력 */
    static void log(boolean value){if(Common.getInstance().isTest)Log.d(Common.getInstance().TAG,Boolean.toString(value));}

    /**
     * 현재 시각을 문자열로 반환
     * @return 2016-01-01 12:34:56
     */
    String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 장치 고유 ID를 생성해 deviceId 변수에 저장함
     * @param context context
     */
    public void setUniqueID(Context context){
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            setPreference(context,"deviceId",deviceUuid.toString());
        } catch(Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * 기기 저장값 가져오기
     * @param context context
     * @param prefKey key
     * @return value
     */
    static public String getPreference(Context context,String prefKey){
        SecurePreferences preferences = new SecurePreferences(context, preferenceName,secureKey, true);
        String result = preferences.getString(prefKey);
        if(result==null)result="";
        return result;
    }

    /**
     * 기기 저장값 저장
     * @param context context
     * @param prefKey key
     * @param prefValue value
     */
    static public void setPreference(Context context,String prefKey,String prefValue){
        SecurePreferences preferences = new SecurePreferences(context, preferenceName,secureKey, true);
        preferences.put(prefKey,prefValue);
    }

    /**
     * 데이터 전송
     * @param calltype 호출 종류를 구분하기 위한 상수
     * @param url 호출 주소
     * @param params 변수들
     * @param iDataHandler 전송 후 처리할 핸들러
     */
    public boolean loadData(int calltype,String url,Object[][] params,IDataHandler iDataHandler){
        if(getPreference(context,"deviceId").equals(""))return false;
        dataHandler = iDataHandler;
        HttpAsyncTask hat = new HttpAsyncTask();
        hat.calltype=calltype;
        hat.datas =params;
        hat.execute(url);
        return true;
    }

    /**
     * 데이터 전송
     * @param calltype 호출 종류를 구분하기 위한 상수
     * @param url 호출 주소
     * @param params 변수들
     */
    public void loadData(int calltype,String url,Object[][] params){
        dataHandler = null;
        HttpAsyncTask hat = new HttpAsyncTask();
        hat.calltype=calltype;
        hat.datas =params;
        hat.execute(url);
    }

    /**
     * 데이터 전송을 위한 class
     */
    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        static final int CALLTYPE_ADDREMOVE_SAVE = 1;
        static final int CALLTYPE_INSTALLEDAPP_SAVE = 2;
        static final int CALLTYPE_ACTIVITY_SAVE = 3;
        static final int CALLTYPE_TRAFFIC_SAVE = 4;
        static final int CALLTYPE_USERINFO = 5;

        public int calltype;
        public Object[][] datas;
        private String params = "";
        @Override
        protected String doInBackground(String... urls){
            try {
                //기본적으로 deviceId 전달
                params = "";
                params += "uid=" + URLEncoder.encode(getPreference(context,"deviceId"), "UTF-8");
                //추가로 입력받은 파라메터가 있다면 전달
                if(datas!=null) {
                    int i;
                    Object[] data;
                    String key, value;
                    for (i = 0; i < datas.length; i++) {
                        data = datas[i];
                        if(data[0]==null)continue;
                        else key = (String) data[0];
                        if(data[1]==null)value="";
                        else value = (String) data[1];
                        params += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
                    }
                }
            } catch(Exception e){
                Common.log(e.toString());
                params = "";
            }
            return GET(urls[0],params);
        }
        @Override
        protected void onPostExecute(String result){
            String err = "";
            String toast = "";
            try {
                JSONObject json = new JSONObject(result);
                err = json.getString("ERR");
                //출력해야 할 토스트가 있다면 출력함
                if(json.has("TOAST")){
                    toast = json.getString("TOAST");
                    if(activity!=null)if(!toast.equals("")) Toast.makeText(activity, toast, Toast.LENGTH_SHORT).show();
                }
            } catch(Exception e){
                err = e.toString();
            }
            Common.log(err);
            switch(err){
                case "LOGOUT":
                    if(activity!=null) {
                        //Intent intent = new Intent(activity, LoginActivity.class);
                        //activity.startActivityForResult(intent, 0);
                    }
                    break;
                default:
                    if(dataHandler!=null)dataHandler.dataHandler(calltype,result);
            }
        }
    }
    private String GET(String targetURL,String urlParameters){
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"ERR\":\"Exception1\"}";
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 시간(long)을 초/분/시간/일로 반환
     * @param time 입력시간
     * @return 반환값
     */
    public String timeToStr(long time){
        String ret = "";
        time/=1000;
        if(time<60){
            ret = Long.toString(time)+"초";
            return ret;
        }
        time/=60;
        if(time<1000){
            ret = Long.toString(time)+"분";
            return ret;
        }
        time/=60;
        if(time<100){
            ret = Long.toString(time)+"시간";
            return ret;
        }
        time/=24;
        ret = Long.toString(time)+"일";
        return ret;
    }
}
