package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//Smover 기기 DB 추가 등록 Http
public class GetIndexHttp {
    private static GetIndexHttp instance = new GetIndexHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/GetIndex.php";

    public static GetIndexHttp getInstance() { return instance; }

    private GetIndexHttp(){ this.client = new OkHttpClient(); }

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