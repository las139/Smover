package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//로그인 DB 체크하는 Http
public class LoginCheckHttp {
    private static LoginCheckHttp instance = new LoginCheckHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/LoginCheck.php";

    public static LoginCheckHttp getInstance() { return instance; }

    private LoginCheckHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String pw, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("u_id", id)
                .add("u_pw", pw)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}