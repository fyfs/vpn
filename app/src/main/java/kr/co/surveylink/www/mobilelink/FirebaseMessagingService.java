package kr.co.surveylink.www.mobilelink;

/**
 * Created by jsyang on 2016-09-27.
 */
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        JSONObject json;
        try {
            json = new JSONObject(remoteMessage.getData().get("message"));
            String cmd=json.getString("cmd");
            if(cmd==null)return;
            switch(cmd){
                case "push":
                    sendPushNotification(json);
                    break;
                case "vpnreset":
                    ToyVpnService.needRestart=true;
                    break;
                case "savenow":
                    //당장 저장
                    String target = json.getString("target");
                    switch(target){
                        case "MActivity":
                            MActivity.getInstance().save(getApplicationContext());
                            break;
                        case "MAddRemove":
                            MAddRemove.getInstance().save(getApplicationContext());
                            break;
                        case "MInstalledApp":
                            MInstalledApp.getInstance().checkInstalledApp(getApplicationContext());
                            MInstalledApp.getInstance().save(getApplicationContext());
                            break;
                    }
                    break;
            }
        } catch(Exception e){
            Common.log(e.toString());
        }
    }

    /**
     * 푸시 화면에 표시
     * @param json json data(title,content가 있어야 함)
     */
    private void sendPushNotification(JSONObject json) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.common_plus_signin_btn_text_light).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(Common.getInstance().isNull(json.getString("title")))
                    .setContentText(Common.getInstance().isNull(json.getString("content")))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri).setLights(000000255, 500, 2000)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            wakelock.acquire(5000);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (Exception e){
            //권한실패가 있을라나
            MPermissions.getInstance().permissionChanged(getApplicationContext(),MPermissions.NEED_FIREBASE_CLOUD_MESSAGE,true);
            Common.log(e.toString());
        }
    }

}