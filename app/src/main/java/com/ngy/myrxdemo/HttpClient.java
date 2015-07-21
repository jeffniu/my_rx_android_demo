package com.ngy.myrxdemo;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by niuguangyuan on 7/20/2015.
 */
public enum HttpClient {

    INSTANCE;

    private OkHttpClient mClient;

    HttpClient() {
        mClient = new OkHttpClient();
    }

    public String getCall(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = mClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return "";
        }
    }


}
