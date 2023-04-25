package com.example.las13.smoverproject.HttpConnection;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

//Smover DB 中 SMO_DATA테이블 수정하는 Http
public class EditSmoverHttp {
    private static EditSmoverHttp instance = new EditSmoverHttp();
    private OkHttpClient client;
    private String url = "http://172.16.12.138:8080/EditSmover.php";

    public static EditSmoverHttp getInstance() { return instance; }

    private EditSmoverHttp(){ this.client = new OkHttpClient(); }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String id, String no, String name, String address, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("no", no)
                .add("name", name)
                .add("address", address)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

}