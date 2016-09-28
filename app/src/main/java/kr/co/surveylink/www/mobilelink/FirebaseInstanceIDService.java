package kr.co.surveylink.www.mobilelink;

/**
 * Created by jsyang on 2016-09-27.
 */

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        Common.log("----A");
        MUserinfo.getInstance().savePushToken(getApplicationContext());
    }

}