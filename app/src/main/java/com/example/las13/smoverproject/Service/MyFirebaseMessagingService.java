package com.example.las13.smoverproject.Service;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.example.las13.smoverproject.Activity.MainActivity;
import com.example.las13.smoverproject.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static int NOTI_ID = 777;
    private boolean push_flag = true;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences( this);

        boolean pushOnOffChecked = prefs.getBoolean("key_PushAlert", true);
        boolean pushTimeChecked = prefs.getBoolean("key_PushAlertTime", false);

        Map<String, String> pushDataMap = remoteMessage.getData();

        if(pushOnOffChecked) {   //푸시 알림 수신 여부 체크했을 경우
            if(pushTimeChecked) {   //푸쉬 알림 방해금지 체크했을 경우
                // 푸쉬 알림 방해금지 설정 시간 불러오기
                int startH = Integer.parseInt(StartTimeHLoad());
                int startM = Integer.parseInt(StartTimeMLoad());
                int endH = Integer.parseInt(EndTimeHLoad());
                int endM = Integer.parseInt(EndTimeMLoad());

                // 현재 날짜 구하기
                String getDate = getNowDate();
                String strings[] = getDate.split("-");
                int nowH = Integer.parseInt(strings[0]);
                int nowM = Integer.parseInt(strings[1]);

                //현재 시간이 방해 금지 시간대인지 확인
                if (startH < nowH && nowH < endH)
                    push_flag = false;
                else {
                    if (startH == nowH && nowH < endH) {
                        if (startM <= nowM)
                            push_flag = false;
                    } else if (startH < nowH && nowH == endH) {
                        if (nowM <= endM)
                            push_flag = false;
                    } else if (startH == nowH && nowH == endH) {
                        if (startM <= nowM && nowM <= endM)
                            push_flag = false;
                    }
                }
                //방해금지 시간이 아닐 경우에만 push 알림 수신
                if (push_flag) {
                    sendNotification(pushDataMap);
                }
            }
            else{    //푸쉬 알림 방해금지 체크 안했을 경우 push 알림 수신
                sendNotification(pushDataMap);
            }
        }
    }

    private void sendNotification(Map<String, String> dataMap) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("SmokeNo", SmokeNoLoad());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(dataMap.get("title"))
                .setContentText(dataMap.get("message"))
                .setAutoCancel(true)
                //.setSound(defaultSoundUri)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_sound)).setLights(000000255,500,2000)
                .setVibrate(new long[]{1000, 1000})
                .setLights(Color.WHITE, 1500, 1500)
                .setContentIntent(contentIntent);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTI_ID /* ID of notification */, nBuilder.build());
    }

    private String getNowDate(){
        long now = System.currentTimeMillis();  // 현재 시간 가져오기
        Date date = new Date(now);  // Date 생성하기
        SimpleDateFormat mFormat = new SimpleDateFormat("hh-mm"); //가져오고 싶은 형식으로 가져오기
        return mFormat.format(date);
    }

    //흡연 감지된 Smover 번호 불러오기 (SharedPreference)
    private String SmokeNoLoad() {
        SharedPreferences pref = getSharedPreferences("SmokeNo", Activity.MODE_PRIVATE);
        return pref.getString("No", "");
    }

    //푸쉬 알림 방해 금지 시간대 시작 시간 불러오기 - 시 (SharedPreference)
    private String StartTimeHLoad() {
        SharedPreferences pref = this.getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("StartTimeH", "0");
    }
    //푸쉬 알림 방해 금지 시간대 시작 시간 불러오기 - 분 (SharedPreference)
    private String StartTimeMLoad() {
        SharedPreferences pref = this.getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("StartTimeM", "0");
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 불러오기 - 시 (SharedPreference)
    private String EndTimeHLoad() {
        SharedPreferences pref = this.getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("EndTimeH", "0");
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 불러오기 - 분 (SharedPreference)
    private String EndTimeMLoad() {
        SharedPreferences pref = this.getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("EndTimeM", "0");
    }
}