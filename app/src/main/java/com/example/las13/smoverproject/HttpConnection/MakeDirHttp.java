package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//서버에 디렉터리 생성하는 Http
public class MakeDirHttp {
    private static MakeDirHttp instance = new MakeDirHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:80/web/SmoverProject/MakeDir.jsp";

    public static MakeDirHttp getInstance() { return instance; }

    private MakeDirHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String no, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(url + "?id=" + id + "&no=" + no)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}