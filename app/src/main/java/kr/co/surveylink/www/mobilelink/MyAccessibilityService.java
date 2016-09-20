package kr.co.surveylink.www.mobilelink;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by jsyang on 2016-09-13.
 */
public class MyAccessibilityService extends AccessibilityService {
    public static String sActivityName;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            Log.d("activitytest", "onAccessibilityEvent with window state changed");
            sActivityName = event.getClassName().toString();
            Common.log(sActivityName);
        }
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        Common.log("onInterrupt");
    }

}

