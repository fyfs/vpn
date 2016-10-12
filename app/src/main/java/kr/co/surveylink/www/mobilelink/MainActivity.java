package kr.co.surveylink.www.mobilelink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 앱 시작 activity
 */
public class MainActivity extends Activity implements View.OnClickListener {

    /** 권한 요청할 때 [다시보지않기] 를 누른 경우 onRequestPermissionsResult 에 자동으로 false가 반환되기 때문에 클릭한 시간을 확인해 분기 처리함 */
    private long clickTime;

    MServiceMonitor mServiceMonitor = MServiceMonitor.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //종료시 자동 재시작 기능
        if(mServiceMonitor.isMonitoring()==false) {
            mServiceMonitor.startMonitoring(getApplicationContext());
            startService(new Intent(this, ToyVpnService.class));
        }
        Common.getInstance().context=getApplicationContext();

    }

    @Override
    protected void onResume(){
        super.onResume();
        start();
    }

    /** 초기화 시작 */
    void start(){

        boolean permissionOk = true;
        Intent vpnIntent = VpnService.prepare(this);

        Button btn_permission = (Button)findViewById(R.id.btn_permissions);
        Button btn_usageAccess = (Button)findViewById(R.id.btn_usageAccess);
        Button btn_vpn = (Button)findViewById(R.id.btn_vpn);

        //기본 권한확인
        if(MPermissions.getInstance().needPermissions(getApplicationContext())) {
            btn_permission.setVisibility(View.VISIBLE);
            permissionOk=false;
        } else {
            btn_permission.setVisibility(View.GONE);
        }

        //앱 사용정보 권한 확인
        if(MPermissions.getInstance().needUsageAccess(getApplicationContext())){
            btn_usageAccess.setVisibility(View.VISIBLE);
            permissionOk=false;
        } else {
            btn_usageAccess.setVisibility(View.GONE);
        }

        //Vpn 권한 확인
        if (vpnIntent != null) {
            MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_VPN,true);
            btn_vpn.setVisibility(View.VISIBLE);
            permissionOk=false;
            ToyVpnService.hasPermission=false;
        } else {
            MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_VPN,false);
            btn_vpn.setVisibility(View.GONE);
            ToyVpnService.hasPermission=true;
        }

        //푸시 토큰 저장
        MUserinfo.getInstance().savePushToken(getApplicationContext());

        //권한 상태
        setCurrentStateOk(permissionOk);
        if(!permissionOk)return;

        //로그인이 되어있다면 아이디 표시
        if(!Common.getPreference(getApplicationContext(),"deviceId").equals("")){
            EditText et_uid=(EditText)findViewById(R.id.et_uid);
            et_uid.setText(Common.getPreference(getApplicationContext(),"deviceId"));
        }

        //테스트용 아이디라면 보이게 할 버튼들 처리
        Button btn_reset=(Button)findViewById(R.id.btn_reset);
        Button btn_action=(Button)findViewById(R.id.btn_action);
        if(Common.getPreference(getApplicationContext(),"deviceId").length()>=6 && Common.getPreference(getApplicationContext(),"deviceId").substring(0,6).equals("tester")){
            btn_reset.setVisibility(View.VISIBLE);
            btn_action.setVisibility(View.VISIBLE);
        } else {
            btn_reset.setVisibility(View.GONE);
            btn_action.setVisibility(View.GONE);
        }

        //설치된 앱들 확인
        MInstalledApp.getInstance().checkInstalledApp(getApplicationContext());

        //설치된 앱 개수 화면에 표시
        setInstalledAppCount();

        //기본정보 저장
        MUserinfo.getInstance().saveBasicInfo(getApplicationContext());

        //통계정보 정리
        MStatistics.getInstance().cleanStat();

        //가장 자주 실행한 앱 화면에 표시
        setFreqApp();

        //가장 오래 실행한 앱 화면에 표시
        setLongApp();

        //장치 사용시간 표시
        setUseTime();

        //현재 네트워크 상태 저장
        MCellWifi.getInstance().curNetwork=MCellWifi.getInstance().getNetworkState(getApplicationContext());

        //서비스 시작
        if(!Common.getInstance().isMyServiceRunning(MServiceMonitor.MService.class))startService(new Intent(this, MServiceMonitor.MService.class));
        if(!Common.getInstance().isMyServiceRunning(ToyVpnService.class))startService(new Intent(this, ToyVpnService.class));
        if(!Common.getInstance().isMyServiceRunning(FirebaseMessagingService.class))startService(new Intent(this, FirebaseMessagingService.class));
        if(!Common.getInstance().isMyServiceRunning(FirebaseInstanceIDService.class))startService(new Intent(this, FirebaseInstanceIDService.class));

    }

    /**
     * 현재 상태 화면에 표시
     * @param isNormal 정상 여부
     */
    private void setCurrentStateOk(boolean isNormal){
        TextView tv_currentState = (TextView)findViewById(R.id.tv_currentState);
        TextView tv_needPermission = (TextView)findViewById(R.id.tv_needPermission);
        tv_currentState.setText(getString(isNormal?R.string.lbl_currentState_normal:R.string.lbl_currentState_abnormal));
        tv_currentState.setTextColor(isNormal?Color.GREEN:Color.RED);
        tv_needPermission.setVisibility(isNormal?View.GONE:View.VISIBLE);
    }

    /**
     * 설치된 앱 개수 화면에 표시
     */
    private void setInstalledAppCount(){
        TextView tv_installedApp = (TextView)findViewById(R.id.tv_installedApp);
        tv_installedApp.setText(Integer.toString(MStatistics.getInstance().appNames.size()));
    }

    /**
     * 가장 자주 실행한 앱 표시
     */
    private void setFreqApp(){
        TextView tv_freqApp = (TextView)findViewById(R.id.tv_freqApp);
        tv_freqApp.setText(MStatistics.getInstance().getFreqApp(getApplicationContext()));
    }

    /**
     * 가장 오래 실행한 앱 표시
     */
    private void setLongApp(){
        TextView tv_longApp = (TextView)findViewById(R.id.tv_longApp);
        tv_longApp.setText(MStatistics.getInstance().getLongApp(getApplicationContext()));
    }

    /**
     * 장치 사용시간 표시
     */
    private void setUseTime(){
        TextView tv_usetime = (TextView)findViewById(R.id.tv_usetime);
        tv_usetime.setText(MStatistics.getInstance().getUseTime(getApplicationContext()));
    }

    /**
     * 버튼 클릭 처리
     * @param v view
     */
    @Override
    public void onClick(View v){
        clickTime= System.currentTimeMillis();
        switch(v.getId()){
            //일반 권한 허용 버튼 클릭
            case R.id.btn_permissions:MPermissions.getInstance().requestPermissions(this);break;
            //앱 사용정보 권한 허용 버튼 클릭
            case R.id.btn_usageAccess:requestUsageAccess();break;
            //VPN 설정 클릭
            case R.id.btn_vpn:
                Intent intent = VpnService.prepare(this);
                if (intent != null) {
                    MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_VPN,true);
                    startActivityForResult(intent, 0);
                } else {
                    MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_VPN,false);
                    startService(new Intent(this, ToyVpnService.class));
                }
                break;
            //로그인
            case R.id.btn_login:
                login();
                break;
            //통계자료 초기화
            case R.id.btn_reset:
                MStatistics.getInstance().reset();
                start();
                ToyVpnService.needRestart=true;
                Common.log("click");
                break;
            //테스트 실행 버튼 클릭
            case R.id.btn_action:
                //MInstalledApp.getInstance().checkInstalledApp(getApplicationContext());
                //MInstalledApp.getInstance().save(getApplicationContext());
                Toast.makeText(getApplicationContext(),Integer.toString(Common.getInstance().toastTestInt)+":"+Common.getInstance().toastTestValue,Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 권한 변경시
     * @param requestCode 요청 코드
     * @param permissions 변경된 권한들
     * @param grantResults 부여된 권한들
     */
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

    /**
     * 사용정보 접근 허용 요청
     */
    private void requestUsageAccess(){
        try {
            Toast.makeText(MainActivity.this,getString(R.string.usageAccess), Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } catch (Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * Vpn 연결시
     * @param request request
     * @param result result
     * @param data intent data
     */
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            String prefix = getPackageName();
            Intent intent = new Intent(this, ToyVpnService.class);
            startService(intent);
            Button btn_vpn = (Button)findViewById(R.id.btn_vpn);
            btn_vpn.setVisibility(View.GONE);
        }
    }

    /**
     * 로그인
     */
    private void login() {
        String uid="";
        EditText et_uid=(EditText)findViewById(R.id.et_uid);
        uid=et_uid.getText().toString();
        Common.setPreference(getApplicationContext(),"deviceId",uid);
        Common.getInstance().hideKeyboard(getApplicationContext(),et_uid);
        Toast.makeText(getApplicationContext(),R.string.loginSuccess,Toast.LENGTH_SHORT).show();
    }

}