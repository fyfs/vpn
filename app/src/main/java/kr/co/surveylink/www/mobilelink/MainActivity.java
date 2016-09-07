package kr.co.surveylink.www.mobilelink;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    MServiceMonitor mServiceMonitor = MServiceMonitor.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //종료시 자동 재시작 기능
        if(mServiceMonitor.isMonitoring()==false) {
            mServiceMonitor.startMonitoring(getApplicationContext());
        }

        //월별 설치된 앱 확인
        //MInstalledApp.getInstalledApp(this.getApplicationContext());

        //위치(위도,경도,고도,정확도)
        //MLocation.getGps(getApplicationContext());

    }

}