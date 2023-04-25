package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//서버에 있는 smover 대표사진, 흡연 사진 폴더 삭제하는 Http
public class DelDirHttp {
    private static DelDirHttp instance = new DelDirHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:80/web/SmoverProject/DeleteDir.jsp";

    public static DelDirHttp getInstance() { return instance; }

    private DelDirHttp(){ this.client = new OkHttpClient(); }

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