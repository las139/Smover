package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//Smover 대표 사진 가져와 편집 다이얼로그에 불러오는 Http
public class LoadSmoImgHttp {
    private static LoadSmoImgHttp instance = new LoadSmoImgHttp();
    private OkHttpClient client;

    public static LoadSmoImgHttp getInstance() { return instance; }

    private LoadSmoImgHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String img_dir, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(img_dir)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}