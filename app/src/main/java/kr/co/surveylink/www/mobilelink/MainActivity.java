package kr.co.surveylink.www.mobilelink;

import android.net.VpnService;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VpnService.prepare(this.getApplicationContext());
    }
}
