package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//토큰값 DB에 저장하는 Http
public class RegTokenHttp {
    private static RegTokenHttp instance = new RegTokenHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/RegisterToken.php";

    public static RegTokenHttp getInstance() { return instance; }

    private RegTokenHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String token, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("token", token)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}