package com.experiment.mslab.posttest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView text_view;
    private TextView text_view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_view = (TextView)findViewById(R.id.textView);
        text_view2 = (TextView)findViewById(R.id.textView2);

        Button btn_bcon1 = (Button)findViewById(R.id.btn_bcon1);
        Button btn_bcon2 = (Button)findViewById(R.id.btn_bcon2);
        Button btn_bcon3 = (Button)findViewById(R.id.btn_bcon3);
        Button btn_done = (Button)findViewById(R.id.btn_done);


        btn_bcon1.setOnClickListener(bconListener);
        btn_bcon2.setOnClickListener(bconListener);
        btn_bcon3.setOnClickListener(bconListener);
        btn_done.setOnClickListener(bconListener);
    }

    private Button.OnClickListener bconListener = new Button.OnClickListener() {
        public void onClick(View v) {
            String[] pkDataKey = {"user_id", "route_id", "bpre", "bcur"};
            String[] pkDataValue = {"12345", "123", "", ""};
            int[] dir = new int[1];
            int[] dis = new int[1];

            switch (v.getId()) {
                case R.id.btn_bcon1:
                    text_view2.setText("Beacon1");
                    Toast.makeText(MainActivity.this, "Beacon1", Toast.LENGTH_SHORT).show();
                    pkDataValue[2] = "start";
                    pkDataValue[3] = "bcon1";
                    dir = new int[]{0};
                    dis = new int[]{1};
                    break;

                case R.id.btn_bcon2:
                    text_view2.setText("Beacon2");
                    Toast.makeText(MainActivity.this, "Beacon2", Toast.LENGTH_SHORT).show();
                    pkDataValue[2] = "bcon1";
                    pkDataValue[3] = "bcon2";
                    dir = new int[]{0, 270, 0};
                    dis = new int[]{2, 3, 2};
                    break;

                case R.id.btn_bcon3:
                    text_view2.setText("Beacon3");
                    Toast.makeText(MainActivity.this, "Beacon3", Toast.LENGTH_SHORT).show();
                    pkDataValue[2] = "bcon2";
                    pkDataValue[3] = "bcon3";
                    dir = new int[]{0, 90};
                    dis = new int[]{6, 5};
                    break;

                case R.id.btn_done:
                    text_view2.setText("Arrive");
                    Toast.makeText(MainActivity.this, "Arrive", Toast.LENGTH_SHORT).show();
                    pkDataValue[2] = "bcon3";
                    pkDataValue[3] = "7-11";
                    dir = new int[]{0, 270};
                    dis = new int[]{4, 3};
                    break;
            }

            try {
                JSONArray sgList = new JSONArray();

                for(int i = 0; i < dir.length; i++) {
                    JSONObject sg = new JSONObject();
                    sg.put("direction", dir[i]);
                    sg.put("distance", dis[i]);
                    sgList.put(sg);
                }

                final JSONObject pkData = new JSONObject();
                for(int i =  0; i < 4; i++){
                    pkData.put(pkDataKey[i], pkDataValue[i]);
                }
                pkData.put("sgList", sgList);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            URL url = new URL("http://163.13.127.174:9000/postTraj");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setConnectTimeout(5000);
                            conn.setRequestProperty("Content-Type", "application/json");
                            byte[] data = pkData.toString().getBytes();
                            conn.setRequestProperty("Content-length", String.valueOf(data.length));
                            conn.getOutputStream().write(data);

                            int code = conn.getResponseCode();
                            if (code == 200) {
                                InputStream is = conn.getInputStream();
                                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                StringBuffer sb = new StringBuffer();
                                String len = null;

                                while ((len = br.readLine()) != null) {
                                    sb.append(len);
                                }

                                String result = sb.toString();

                                runToastInAnyThread(result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void runToastInAnyThread(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject;
                JSONArray naviArray;
                String status, naviResult = "";
                try {
                    jsonObject = new JSONObject(result);
                    status = jsonObject.get("status").toString();
                    naviArray = jsonObject.getJSONArray("navi");
                    for (int i = 0; i < naviArray.length(); i++) {
                        JSONArray sgList = naviArray.getJSONArray(i);
                        naviResult += "Step" + i + ":\n";
                        for (int j = 0; j < sgList.length(); j++) {
                            JSONObject sg = sgList.getJSONObject(j);
                            naviResult += sg + "\n";
                        }
                    }

                    text_view2.setText(text_view2.getText() + " " + status);
//                    text_view.setText(result);
                    text_view.setText(naviResult);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
