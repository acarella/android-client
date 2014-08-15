package com.mifos.sslworkaround;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;

/**
 * Created by antoniocarella on 8/5/14.
 */
public class ClientDetails extends AsyncTask<String, Void, String> {

    public static String mPGSInstanceUrl = "https://10.0.0.6:8443/mifosng-provider/api/v1/pgsclients";

    private String result;

    @Override
    protected String doInBackground(String... clientId) {

        String mPGSInstanceUrl = "https://10.0.0.6:8443/mifosng-provider/api/v1/pgsclients";
        mPGSInstanceUrl = mPGSInstanceUrl + "/" + clientId[0];

        HttpClient client = new HttpsWorkaround().getNewHttpClient();
        HttpGet get = new HttpGet(mPGSInstanceUrl);
        get.setHeader("Content-Type", "application/json");
        get.setHeader("X-Mifos-Platform-TenantId", "default");
        get.setHeader("Authorization", "Basic bWlmb3M6cGFzc3dvcmQ=");

        // Execute HTTP Post Request
        HttpResponse response = null;
        try {
            response = client.execute(get);
            result = new BasicResponseHandler().handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

}