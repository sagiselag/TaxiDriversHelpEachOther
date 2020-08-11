package com.android2.taxidrivershelpeachother.model;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

public class VolleyHandler {
    private static VolleyHandler instance = null;
    private static Object lock = new Object();

    private JSONObject jsonObjectReturnedFromVolley;

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObjectReturnedFromVolley = jsonObject;
    }

    private VolleyHandler(){ }

    public static VolleyHandler getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new VolleyHandler();
                }
            }
        }

        return instance;
    }

    public JSONObject getJsonObjectReturnedFromVolley() {
        return jsonObjectReturnedFromVolley;
    }

    public void getJSONObjectWithVolley(final Context context, String url, final Callable<Void> methodParam){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject newJsonObject = new JSONObject(response);
                            setJsonObject(newJsonObject);
//                            timeAndDistanceResult[httpVolleyRequestCounter++] = onVolleyReturn();
                            //call methodParam
                            try {
                                methodParam.call();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }catch (JSONException e){
                            Toast.makeText(context.getApplicationContext(), "JSON parsing error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), "fetch volley error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
