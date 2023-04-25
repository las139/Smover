package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//SMO_IMG 테이블(흡연 감지 목록) 내용 DB에서 받아오는 Http
public class GetSmoImgAllHttp {
    private static GetSmoImgAllHttp instance = new GetSmoImgAllHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/GetSmoImgAll.php";

    public static GetSmoImgAllHttp getInstance() { return instance; }

    private GetSmoImgAllHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String index, String date, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("index", index)
                .add("date", date)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}