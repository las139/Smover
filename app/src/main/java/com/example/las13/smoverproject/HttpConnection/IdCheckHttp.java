package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//ID 중복확인 DB 체크하는 Http
public class IdCheckHttp {
    private static IdCheckHttp instance = new IdCheckHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/SignUp_IdCheck.php";

    public static IdCheckHttp getInstance() { return instance; }

    private IdCheckHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("u_id", id)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}