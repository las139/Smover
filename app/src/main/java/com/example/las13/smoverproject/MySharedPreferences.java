package com.example.las13.smoverproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MySharedPreferences {
    private Context context= null;

    public MySharedPreferences(Context context){
        this.context = context;
    }

    //자동로그인 상태값 ON으로 저장 (SharedPreference)
    public void AutoCheckBoxSaveOn() {
        SharedPreferences pref = context.getSharedPreferences("AutoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("OnOff", "ON");
        editor.apply();
    }

    //자동로그인 상태값 OFF로 저장 (SharedPreference)
    public void AutoCheckBoxSaveOff() {
        SharedPreferences pref = context.getSharedPreferences("AutoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("OnOff", "OFF");
        editor.apply();
    }
    //자동로그인 상태값 불러오기 (SharedPreference)
    public String AutoLoginCheckLoad() {
        SharedPreferences pref = context.getSharedPreferences("AutoLogin", Activity.MODE_PRIVATE);
        return pref.getString("OnOff", "OFF");
    }

    //사용자의 id 저장 (SharedPreference)
    public void UserIdSave(String id) {
        SharedPreferences pref = context.getSharedPreferences("UserId", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Id", id);
        editor.apply();
    }
    //사용자의 id 불러오기 (SharedPreference)
    public String UserIdLoad() {
        SharedPreferences pref = context.getSharedPreferences("UserId", Activity.MODE_PRIVATE);
        return pref.getString("Id", "");
    }

    //사용자의 pw 저장 (SharedPreference)
    public void UserPwSave(String id) {
        SharedPreferences pref = context.getSharedPreferences("UserPw", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Pw", id);
        editor.apply();
    }
    //사용자의 pw 불러오기 (SharedPreference)
    public String UserPwLoad() {
        SharedPreferences pref = context.getSharedPreferences("UserPw", Activity.MODE_PRIVATE);
        return pref.getString("Pw", "");
    }

    //흡연감지된 Smover 번호 저장 (SharedPreference)
    public void SmokeNoSave(String no) {
        android.content.SharedPreferences pref = context.getSharedPreferences("SmokeNo", Activity.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.putString("No", no);
        editor.apply();
    }

    //토큰값 불러오기 (SharedPreference)
    public String TokenLoad() {
        SharedPreferences pref = context.getSharedPreferences("Token", Activity.MODE_PRIVATE);
        return pref.getString("Token", "");
    }
}
