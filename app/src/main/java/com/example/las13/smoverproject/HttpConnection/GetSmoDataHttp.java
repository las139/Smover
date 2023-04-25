package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//Smover 대표 사진 가져와 비트맵으로 변환하여 저장하는 Http
public class GetSmoDataHttp {
    private static GetSmoDataHttp instance = new GetSmoDataHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/GetSmoData.php";

    public static GetSmoDataHttp getInstance() {
        return instance;
    }

    private GetSmoDataHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String index, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("u_id", id)
                .add("index", index)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("index", index)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}