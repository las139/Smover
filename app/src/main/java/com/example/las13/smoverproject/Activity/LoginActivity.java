package com.example.las13.smoverproject.Activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.las13.smoverproject.HttpConnection.IdCheckHttp;
import com.example.las13.smoverproject.HttpConnection.LoginCheckHttp;
import com.example.las13.smoverproject.HttpConnection.RegTokenHttp;
import com.example.las13.smoverproject.HttpConnection.SignUpHttp;
import com.example.las13.smoverproject.MySharedPreferences;
import com.example.las13.smoverproject.R;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.example.las13.smoverproject.R.id.auto_login_check;

public class LoginActivity extends Activity {
    private static final String TAG = "#LoginActivity";

    private MySharedPreferences myPreferences = new MySharedPreferences(LoginActivity.this);

    private IdCheckHttp icHttp = IdCheckHttp.getInstance();
    private SignUpHttp suHttp = SignUpHttp.getInstance();
    private LoginCheckHttp lcHttp = LoginCheckHttp.getInstance();
    private RegTokenHttp rtHttp = RegTokenHttp.getInstance();

    private String user_id = null;
    private String user_pw = null;
    private int idcheck_flag = 0;
    private SignupDialog dialog_signup;
    private CheckBox check_autologin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        check_autologin = (CheckBox) findViewById(auto_login_check);
        check_autologin.setChecked(false);

        //id, pw 입력창 밑줄 색상 변경
        int color = Color.parseColor("#CDCDCD");
        final EditText edit_id = (EditText) findViewById(R.id.edit_login_id);
        final EditText edit_pw = (EditText) findViewById(R.id.edit_login_pw);
        edit_id.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        edit_pw.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        //자동로그인 체크 했을 경우 바로 로그인
        if ((myPreferences.AutoLoginCheckLoad()).equals("ON")) {
            check_autologin.setChecked(true);
            //로그인 체크
            new Thread() {
                public void run() {
                    LoginCheckCallback callback = new LoginCheckCallback();
                    lcHttp.requestWebServer(myPreferences.UserIdLoad(), myPreferences.UserPwLoad(), callback);
                }
            }.start();

            Log.d("#LoginActivity", "자동 로그인 체크 됨");
        }

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
        int width = dm.widthPixels; //디바이스 화면 너비

        dialog_signup = new SignupDialog(this);
        WindowManager.LayoutParams wm = dialog_signup.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
        wm.copyFrom(dialog_signup.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
        wm.width = width;

        // 회원가입 버튼 리스너
        TextView tv = (TextView) findViewById(R.id.tv_sign_up);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_signup.show();
            }
        });

        // 로그인 버튼 리스너
        Button btn_login = (Button) this.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editID = (EditText) findViewById(R.id.edit_login_id);
                final EditText editPW = (EditText) findViewById(R.id.edit_login_pw);

                final String id = editID.getText().toString();
                final String pw = editPW.getText().toString();

                if (id.equals("")) {
                    Toast.makeText(LoginActivity.this, "아이디를 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                } else if (pw.equals("")) {
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // 인터넷 연결 체크
                    try {
                        ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkState = cm.getActiveNetworkInfo();
                        if( networkState == null || !networkState.isConnected()){
                            Toast.makeText(LoginActivity.this, "인터넷 연결을 확인하세요", Toast.LENGTH_LONG).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    //로그인 체크
                    new Thread() {
                        public void run() {
                            user_id = editID.getText().toString();
                            user_pw = editPW.getText().toString();
                            LoginCheckCallback callback = new LoginCheckCallback();
                            lcHttp.requestWebServer(id, pw, callback);
                        }
                    }.start();
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //                                        #CallBack
    //----------------------------------------------------------------------------------------------
    //IdCheckCallback
    private class IdCheckCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String result = response.body().string();

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "1":
                            Toast.makeText(LoginActivity.this, "사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            idcheck_flag = 1;
                            break;
                        case "0":
                            Toast.makeText(LoginActivity.this, "이미 가입되어 있는 아이디입니다.", Toast.LENGTH_SHORT).show();
                            idcheck_flag = 1;
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "err: " + result, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //SignUpCallback
    private class SignUpCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String result = response.body().string();

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "1":
                            Toast.makeText(LoginActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "0":
                            Toast.makeText(LoginActivity.this, "이미 존재하는 사용자입니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //LoginCheckCallback
    private class LoginCheckCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String result = response.body().string();

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "1":
                            //토큰 DB에 저장(최초 로그인 시 1회만)
                            if (myPreferences.UserIdLoad().equals("")) {
                                new Thread() {
                                    public void run() {
                                        RegTokenCallback callback = new RegTokenCallback();
                                        rtHttp.requestWebServer(user_id, myPreferences.TokenLoad(), callback);
                                    }
                                }.start();
                            }

                            //자동로그인 체크 여부 내장 DB에 저장
                            if (check_autologin.isChecked()) {
                                myPreferences.AutoCheckBoxSaveOn();
                                Log.d(TAG, "자동로그인 체크 O - DB에 저장");
                            } else {
                                myPreferences.AutoCheckBoxSaveOff();
                                Log.d(TAG, "자동로그인 체크 X - DB에 저장");
                            }

                            //자동 로그인 체크되어 있을 경우에는 저장되어있는 걸로
                            //체크 안 되어있으면 입력되어있는 id,pw 값으로 로그인
                            if (myPreferences.AutoLoginCheckLoad().equals("ON")) {
                                if(user_id == null && user_pw == null) {
                                    user_id = myPreferences.UserIdLoad();
                                    user_pw = myPreferences.UserPwLoad();
                                }

                                //사용자 id,pw 내장 DB에 저장
                                myPreferences.UserIdSave(user_id);
                                myPreferences.UserPwSave(user_pw);
                                Log.d(TAG, "자동로그인체크ON == id: "+user_id+", pw: "+user_pw);
                            } else {
                                //사용자 id,pw 내장 DB에 저장
                                myPreferences.UserIdSave(user_id);
                                myPreferences.UserPwSave(user_pw);
                                Log.d(TAG, "자동로그인체크OFF == id: "+user_id+", pw: "+user_pw);
                            }
                            //로그인에 성공하면 메인 화면으로 이동
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "0":
                            Toast.makeText(LoginActivity.this, "아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(LoginActivity.this, "로그인에 실패하였습니다 " + result, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //RegTokenCallback
    private class RegTokenCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final  String result = response.body().string();

            Log.d(TAG, "Refreshed Token: Result ==>> " + result);
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "1":
                            Log.d(TAG, "Refreshed Token: DB에 저장 성공!!");
                            break;
                        case "0":
                            Log.d(TAG, "Refreshed Token: DB에 저장 실패..");
                            break;
                    }
                }
            });
        }
    }

    //----------------------------------------------------------------------------------------------
    //                                        #Dialog
    //----------------------------------------------------------------------------------------------
    //회원가입 dialog
    private class SignupDialog extends Dialog {
        public SignupDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_signup);     //다이얼로그에서 사용할 레이아웃입니다.

            // 아이디 중복확인 버튼 리스너
            Button btn_idcheck = (Button) findViewById(R.id.btn_signup_idcheck);
            btn_idcheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText edit_id = (EditText) findViewById(R.id.edit_signup_id);

                    if (edit_id.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, "아이디를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Id 중복확인 체크
                        new Thread() {
                            public void run() {
                                IdCheckCallback callback = new IdCheckCallback();
                                icHttp.requestWebServer(edit_id.getText().toString(), callback);
                            }
                        }.start();
                    }
                }
            });

            // 확인 버튼 리스너
            Button btn_ok = (Button) findViewById(R.id.btn_signup_ok);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText edit_id = (EditText) findViewById(R.id.edit_signup_id);
                    EditText edit_pw1 = (EditText) findViewById(R.id.edit_signup_pw1);
                    EditText edit_pw2 = (EditText) findViewById(R.id.edit_signup_pw2);
                    EditText edit_email = (EditText) findViewById(R.id.edit_signup_email);

                    if (edit_id.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, "아이디를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else if (edit_pw1.getText().toString().equals("")
                            || edit_pw2.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else if (edit_email.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, "Email을 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //id 중복확인을 통과한 경우
                        if (idcheck_flag == 1) {
                            user_id = edit_id.getText().toString();
                            user_pw = edit_pw1.getText().toString();
                            final String email = edit_email.getText().toString();

                            if (!edit_pw1.getText().toString().equals(edit_pw2.getText().toString())) {
                                Toast.makeText(LoginActivity.this, "비밀번호가 일치하지 않습니다.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                //회원가입 DB 체크
                                new Thread() {
                                    public void run() {
                                        SignUpCallback callback = new SignUpCallback();
                                        suHttp.requestWebServer(user_id, user_pw, email, callback);
                                    }
                                }.start();
                            }
                        }
                        //id 중복확인을 통과하지 못한 경우
                        else {
                            Toast.makeText(LoginActivity.this, "아이디 중복확인을 눌러주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            // 취소 버튼 리스너
            Button btn_cancel = (Button) findViewById(R.id.btn_signup_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();   //다이얼로그를 닫는 메소드입니다.
                }
            });
        }
    }
}