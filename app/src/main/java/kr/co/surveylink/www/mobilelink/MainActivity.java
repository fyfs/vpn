package kr.co.surveylink.www.mobilelink;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 앱 시작 activity
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private long clickTime;
    MServiceMonitor mServiceMonitor = MServiceMonitor.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //종료시 자동 재시작 기능
        if(mServiceMonitor.isMonitoring()==false) {
            mServiceMonitor.startMonitoring(getApplicationContext());
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        start();
    }

    void start(){

        //권한허용 버튼에 이벤트 걸기
        Button btn_permission = (Button)findViewById(R.id.btn_permissions);
        btn_permission.setOnClickListener(this);
        btn_permission.setVisibility(View.GONE);

        //사용정보 접근 허용 버튼에 이벤트 걸기
        Button btn_usageAccess = (Button)findViewById(R.id.btn_usageAccess);
        btn_usageAccess.setOnClickListener(this);
        btn_usageAccess.setVisibility(View.GONE);

        //테스트 실행 버튼
        Button btn_action = (Button)findViewById(R.id.btn_action);
        btn_action.setOnClickListener(this);

        //권한확인
        if(MPermissions.getInstance().needPermissions(getApplicationContext())) {
            btn_permission.setVisibility(View.VISIBLE);
        } else if(MPermissions.getInstance().needUsageAccess(getApplicationContext())){
            btn_usageAccess.setVisibility(View.VISIBLE);
        } else {
            Common.getInstance().setUniqueID(this);
        }

    }

    //클릭 처리
    @Override
    public void onClick(View v){
        clickTime= System.currentTimeMillis();
        switch(v.getId()){
            //권한 허용 버튼 클릭
            case R.id.btn_permissions:MPermissions.getInstance().requestPermissions(this);break;
            //권한 허용 버튼 클릭
            case R.id.btn_usageAccess:requestUsageAccess();break;
            //테스트 실행 버튼 클릭
            case R.id.btn_action:

                MAddRemove.getInstance().save(getApplicationContext());

                MInstalledApp.getInstance().checkInstalledApp(getApplicationContext());
                MInstalledApp.getInstance().save(getApplicationContext());

                MActivity.getInstance().save(getApplicationContext());

                break;
        }
    }

    //권한 변경시
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        long now = System.currentTimeMillis();
        //이렇게 빨리 권한클릭을 완료했다는 것은 [다시 묻지 않기]를 체크했기 때문일테니 앱 설정 페이지로 보내서 허용을 받아내야 한다
        if(clickTime+100>now){
            Toast.makeText(MainActivity.this,getString(R.string.permission), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //사용정보 접근 허용 요청
    private void requestUsageAccess(){
        Context context = getApplicationContext();
        Toast.makeText(MainActivity.this,getString(R.string.usageAccess), Toast.LENGTH_LONG).show();
        try {
            Toast.makeText(MainActivity.this,getString(R.string.usageAccess), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

}