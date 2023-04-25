package com.example.las13.smoverproject.HttpConnection;

import java.util.ArrayList;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//해당 경로에 있는 사진 가져와 비트맵으로 변환하여 저장하는 Http
public class GetImgToBitmapHttp {
    private static GetImgToBitmapHttp instance = new GetImgToBitmapHttp();
    private OkHttpClient client;

    public static GetImgToBitmapHttp getInstance() { return instance; }

    private GetImgToBitmapHttp(){ this.client = new OkHttpClient(); }

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