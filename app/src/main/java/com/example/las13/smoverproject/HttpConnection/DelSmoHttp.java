package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//해당 Smover DB에서 삭제하는 Http
public class DelSmoHttp {
    private static DelSmoHttp instance = new DelSmoHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/DeleteSmover.php";

    public static DelSmoHttp getInstance() { return instance; }

    private DelSmoHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String no, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("no", no)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}