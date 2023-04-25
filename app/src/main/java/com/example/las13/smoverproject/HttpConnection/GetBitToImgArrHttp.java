package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//흡연 감지 목록 사진 가져와 비트맵으로 변환하여 저장하는 Http
public class GetBitToImgArrHttp {
    private static GetBitToImgArrHttp instance = new GetBitToImgArrHttp();
    private OkHttpClient client;

    public static GetBitToImgArrHttp getInstance() { return instance; }

    private GetBitToImgArrHttp(){ this.client = new OkHttpClient(); }

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