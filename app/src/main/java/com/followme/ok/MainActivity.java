package com.followme.ok;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private static String domain = "api url";
    private final String LOGIN = "login url string";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream inputStream = getApplicationContext().getResources().openRawResource(R.raw.kvbinvite);
        OKHttpUtil httpUtil = new OKHttpUtil(inputStream);
        httpUtil.setFormBodyBuilder();
        httpUtil.buildParameter("Login", "account");
        httpUtil.buildParameter("Password", "pwd");

        HashMap<String, String> maps = new HashMap<>();
        maps.put("Authorization", "");

        Request request = new Request.Builder()
                .url(domain + LOGIN)
                .post(httpUtil.getFormBodyBuilder().build()) // here we use put
                .headers(Headers.of(maps))
                .build();

        httpUtil.newAsync(request, new ICallbackListener() {
            @Override
            public void onResult(boolean isSuccess, @NotNull String resultString) {
                Log.i(TAG, isSuccess + " : " + resultString);
                if(isSuccess){
                    try {
                        JSONObject jo = new JSONObject(resultString);
                        Log.i(TAG, jo.optJSONObject("Types").optString("Account"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Log.e(TAG, false + " : " + resultString);
                }

            }

        });

    }
}
