package com.example.las13.smoverproject.Fragment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.las13.smoverproject.Activity.LoginActivity;
import com.example.las13.smoverproject.MySharedPreferences;
import com.example.las13.smoverproject.R;


public class SettingFragment extends PreferenceFragment {
    private MySharedPreferences myPreferences = new MySharedPreferences(getActivity());
    private String startTime_H, startTime_M;
    public static boolean push_flag = true;
    public static SwitchPreference pref1;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        // 푸쉬 알림 방해 시간대 설정 리스너
        Preference pref1 = (Preference) findPreference("key_PushAlertTimeSet");
        pref1.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //설정한 시작 시간 불러오기
                int start_hour = Integer.parseInt(StartTimeHLoad());
                int start_minute = Integer.parseInt(StartTimeMLoad());

                TimePickerDialog dialog = new TimePickerDialog(getActivity(),
                        listener_start, start_hour, start_minute, false);
                dialog.show();

                Toast.makeText(getActivity(), "방해금지를 시작할 시간을 설정해주세요.",
                        Toast.LENGTH_LONG).show();

                return false;
            }
        });

        // 로그아웃 리스너
        Preference pref2 = (Preference) findPreference("key_Logout");
        pref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AutoCheckBoxSaveOff();

                getActivity().finish();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                return false;
            }
        });
    }

    //----------------------------------------------
    //  푸쉬 알림 방해 시간대 설정 대화상자 - 시작
    //----------------------------------------------
    private TimePickerDialog.OnTimeSetListener listener_start = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            // 설정버튼 눌렀을 때

            startTime_H = Integer.toString(hour);
            startTime_M = Integer.toString(minute);

            //설정한 종료 시간 불러오기
            int end_hour = Integer.parseInt(EndTimeHLoad());
            int end_minute = Integer.parseInt(EndTimeMLoad());

            TimePickerDialog dialog = new TimePickerDialog(getActivity(), listener_end, end_hour, end_minute + 1, false);
            dialog.show();

            Toast.makeText(getActivity(), "방해금지를 종료할 시간을 설정해주세요.",
                    Toast.LENGTH_LONG).show();
        }
    };

    //--------------------------------------------------
    //  푸쉬 알림 방해금지 시간대 설정 대화상자 - 종료
    //--------------------------------------------------
    private TimePickerDialog.OnTimeSetListener listener_end = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            // 설정버튼 눌렀을 때

            //논리적으로 시간 설정이 맞지 않을 경우
            if( Integer.parseInt(startTime_H) > hour
                    || (Integer.parseInt(startTime_H) == hour && Integer.parseInt(startTime_M) > minute)
                    || (Integer.parseInt(startTime_H) == hour && Integer.parseInt(startTime_M) == minute) ){
                Toast.makeText(getActivity(), "시간을 잘못 설정하였습니다.",
                        Toast.LENGTH_SHORT).show();
            }
            else{
                //방해금지 시작 시간 저장
                StartTimeHSave(startTime_H);
                StartTimeMSave(startTime_M);

                //방해금지 종료 시간 저장
                EndTimeHSave(Integer.toString(hour));
                EndTimeMSave(Integer.toString(minute));

                Toast.makeText(getActivity(), "시작: " + startTime_H + ", " + startTime_M
                                + "종료: " + Integer.toString(hour) + ", " + Integer.toString(minute),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    //푸쉬 알림 방해 금지 시간대 시작 시간 저장 - 시 (SharedPreference)
    private void StartTimeHSave(String time) {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("StartTimeH", time);
        editor.apply();
    }
    //푸쉬 알림 방해 금지 시간대 시작 시간 저장 - 분 (SharedPreference)
    private void StartTimeMSave(String time) {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("StartTimeM", time);
        editor.apply();
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 저장 - 시 (SharedPreference)
    private void EndTimeHSave(String time) {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("EndTimeH", time);
        editor.apply();
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 저장 - 분 (SharedPreference)
    private void EndTimeMSave(String time) {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("EndTimeM", time);
        editor.apply();
    }

    //푸쉬 알림 방해 금지 시간대 시작 시간 불러오기 - 시 (SharedPreference)
    private String StartTimeHLoad() {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("StartTimeH", "0");
    }
    //푸쉬 알림 방해 금지 시간대 시작 시간 불러오기 - 분 (SharedPreference)
    private String StartTimeMLoad() {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("StartTimeM", "0");
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 불러오기 - 시 (SharedPreference)
    private String EndTimeHLoad() {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("EndTimeH", "0");
    }
    //푸쉬 알림 방해 금지 시간대 종료 시간 불러오기 - 분 (SharedPreference)
    private String EndTimeMLoad() {
        SharedPreferences pref = getActivity().getSharedPreferences("PushAlertTime", Activity.MODE_PRIVATE);
        return pref.getString("EndTimeM", "0");
    }

    //자동로그인 상태값 OFF로 저장 (SharedPreference)
    private void AutoCheckBoxSaveOff() {
        SharedPreferences pref = getActivity().getSharedPreferences("AutoLogin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("OnOff", "OFF");
        editor.apply();
    }
}