package com.example.las13.smoverproject.Service;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static android.content.ContentValues.TAG;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Token: " + refreshedToken);

        //토큰 처음 생성시 or 토큰값 갱신될 경우
        if (TokenLoad().equals("") || !TokenLoad().equals(refreshedToken)) {
            if(!UserIdLoad().equals("")) {  //설치 후 처음 로그인 전에 id를 불러올 수 없는 상황 제외
                sendRegistrationToServer(refreshedToken, "http://172.16.12.138:8080/RegisterToken.php");
                Log.d(TAG,"Refreshed Token: MyFirebaseInstanceIDService에서 token task 실행!");
                Log.d(TAG,"Refreshed Token: ID -> " + UserIdLoad());
                Log.d(TAG,"Refreshed Token: Token -> " + refreshedToken);
            }
            TokenSave(refreshedToken);  //토큰 저장
        }
    }
    // [END refresh_token]

    private void sendRegistrationToServer(String token, String url) {
        Log.d(TAG, "Refreshed Token: RegTokenTask 실행됨");

        //토큰값 가져와서 DB에 저장
        new RegTokenTask().execute(token, url);

        /***
         // OKHTTP를 이용해 웹서버로 토큰값을 날려준다.
         OkHttpClient client = new OkHttpClient();
         RequestBody body = new FormBody.Builder()
         .add("Token", token)
         .add("Id", UserIdLoad())
         .build();

         //request
         Request request = new Request.Builder()
         .url("http://las139.dothome.co.kr/RegisterToken.php")
         .post(body)
         .build();

         try {
         client.newCall(request).execute();
         } catch (IOException e) {
         e.printStackTrace();
         }
         ***/
    }

    //----------------------------------------
    //     토큰값 DB에 저장하는 AsyncTask
    //----------------------------------------
    private class RegTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                String id = UserIdLoad();
                String token = params[0];
                String link = params[1];
                String param = "id=" + id + "&token" + token;

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라미터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                /* 서버 -> 안드로이드 파라미터값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                String data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

                return data;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "Refreshed Token: Result: " + result);
            if (result.equals("1")) {
                Log.d(TAG, "Refreshed Token: DB에 저장 성공!!");
            } else {
                Log.d(TAG, "Refreshed Token: DB에 저장 실패..");
            }
        } //ON DUPLICATE KEY UPDATE Token = '$token'
    }

    //사용자의 id 불러오기 (SharedPreference)
    private String UserIdLoad() {
        SharedPreferences pref = getSharedPreferences("UserId", Activity.MODE_PRIVATE);
        return pref.getString("Id", "");
    }

    //토큰값 저장 (SharedPreference)
    private void TokenSave(String token) {
        SharedPreferences pref = getSharedPreferences("Token", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Token", token);
        editor.apply();
    }

    //토큰값 불러오기 (SharedPreference)
    private String TokenLoad() {
        SharedPreferences pref = getSharedPreferences("Token", Activity.MODE_PRIVATE);
        return pref.getString("Token", "");
    }
}