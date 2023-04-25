package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//SMO_DATA 테이블 내용 전부 DB에서 받아오는 Http
public class GetSmoDataAllHttp {
    private static GetSmoDataAllHttp instance = new GetSmoDataAllHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/GetSmoDataAll.php";

    public static GetSmoDataAllHttp getInstance() {
        return instance;
    }

    private GetSmoDataAllHttp(){ this.client = new OkHttpClient(); }

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