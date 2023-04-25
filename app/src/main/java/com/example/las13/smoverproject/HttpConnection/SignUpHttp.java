package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//회원가입시 사용자 정보 DB에 저장하는 Http
public class SignUpHttp {
    private static SignUpHttp instance = new SignUpHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/SignUp.php";

    public static SignUpHttp getInstance() { return instance; }

    private SignUpHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String pw, String email, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("u_id", id)
                .add("u_pw", pw)
                .add("u_email", email)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}