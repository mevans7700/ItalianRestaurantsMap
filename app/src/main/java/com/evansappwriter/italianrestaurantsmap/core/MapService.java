package com.evansappwriter.italianrestaurantsmap.core;

import android.os.Bundle;
import android.text.TextUtils;

import com.evansappwriter.italianrestaurantsmap.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.protocol.HTTP;


/**
 * Created by mark on 4/1/16.
 */
public class MapService {
    private static final String TAG = "TELMATE.SERVICE";
    private static final String BROWSER_KEY = "AIzaSyBsV33tsInHKhBWA1zc6LhKONQwnRE2eeQ";

    private static MapService mInstance = null;

    private static final int TIMEOUT_READ = 60000; // ms
    private static final int TIMEOUT_CONNECT = 15000; // ms

    @SuppressWarnings("ConstantConditions")
    private static final String REST_API = "https://maps.googleapis.com/maps/api/place/textsearch/json";




    public interface OnUIResponseHandler {
        void onSuccess(String payload);
        void onFailure(String errorTitle, String errorText, int dialogId);
    }

    // private constructor prevents instantiation from other classes
    private MapService() {

    }

    /**
     * Creates a new instance of MapService.
     */
    public static MapService getInstance() {

        if (mInstance == null) {
            mInstance = new MapService();
        }

        return mInstance;
    }

    /**
     * *******************************************************************************************************
     */

    private Bundle getAuthBundle() {
        Bundle params = new Bundle();

        params.putString(PARAM_KEY, BROWSER_KEY);

        return params;
    }

    public static String encodeUrl(Bundle params) {
        if (params == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(200);
        boolean first = true;
        Set<String> keySet = params.keySet();

        for (String key : keySet) {
            Object parameter = params.get(key);

            if (!(parameter instanceof String)) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                sb.append('&');
            }
            try {
                sb.append(URLEncoder.encode(key, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
            sb.append('=');
            try {
                sb.append(URLEncoder.encode(params.getString(key), HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Utils.printStackTrace(e);
            }
        }
        return sb.toString();
    }

    public void get(Bundle params, final OnUIResponseHandler handler) {
        Bundle urlParams = getAuthBundle();
        if (params != null) {
            urlParams.putAll(params);
        }

        String uri = REST_API;
        uri += "?" + encodeUrl(urlParams);

        Utils.printLogInfo(TAG, "API URL: " + uri);
        AsyncHttpClient aClient = new AsyncHttpClient();
        aClient.setTimeout(TIMEOUT_READ);
        aClient.get(uri, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Utils.printLogInfo(TAG, "- Successful !: " + statusCode);

                processSuccessRepsonse(handler, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Utils.printLogInfo(TAG, "- Failed !: " + statusCode);

                processFailureRepsonse(handler, responseBody != null ? new String(responseBody) : null, e.toString());
            }
        });
    }

    private void processSuccessRepsonse(OnUIResponseHandler handler, String payload) {
        handler.onSuccess(payload);
    }

    private void processFailureRepsonse(OnUIResponseHandler handler, String payload, String exception) {
        String errorTitle = "";
        String errorText = "";
        int dialogId;
        if (!TextUtils.isEmpty(payload)) {
            BundledData data = new BundledData(Parser.TYPE_PARSER_ERROR);
            //data.setHttpData(payload);
            //Parser.parseResponse(data);


        } else {

        }

        handler.onFailure(errorTitle, errorText, 0);
    }

    // PARAMS >>>>>>>>>

    public static final String PARAM_KEY = "key";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_RADIUS = "radius";
    public static final String PARAM_TYPES = "types";
    public static final String PARAM_SENSOR = "sensor";
    public static final String PARAM_QUERY = "query";
}
