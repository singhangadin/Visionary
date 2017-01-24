package com.example.visionary;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        LocationUpdateListener,
        OnMapReadyCallback {
    private final int REQ_CODE_SPEECH_INPUT = 100;

    private ArrayList<PointData> pointDatas;
    private LocationService mService;
    private boolean mBound = false;

    private GoogleMap mMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pointDatas=new ArrayList<>();
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("hi"));
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
                try
                {   startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                }
                catch (ActivityNotFoundException a)
                {   Toast.makeText(getBaseContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 112);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 112);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService();
            mService.addLocationUpdateListener(MainActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.removeLocationUpdateListener();
            mBound = false;
        }
    };

    @Override
    public void onLocationChanged(Location location) {
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 112);
            return;
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    //Source to Destination
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speech=result.get(0);
                    Log.e("TAG","Result:"+speech);
                    String splitt[]=speech.split("to");
                    String requestUrl= null;
                    try {
                        requestUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "transit_routing_preference=less_walking&" +
                        "mode=walking&" +
                        "origin="+ URLEncoder.encode(splitt[0], "UTF-8")+"&" +
                        "destination="+URLEncoder.encode(splitt[1], "UTF-8")+"&" +
                        "key="+getResources().getString(R.string.MAPS_API_KEY);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsObjRequest = new JsonObjectRequest
                        (Request.Method.GET,
                                requestUrl,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try
                                    {   JSONArray routes = response.getJSONArray("routes");
                                        for(int i = 0; i<routes.length();i++)
                                        {   JSONObject route = routes.getJSONObject(i);
                                            JSONArray legs = route.getJSONArray("legs");
                                            for(int k = 0; k<legs.length(); k++) {
                                                JSONObject leg = legs.getJSONObject(k);
                                                JSONArray steps = leg.getJSONArray("steps");
                                                for (int j = 0; j < steps.length(); j++) {
                                                    JSONObject step = steps.getJSONObject(j);
                                                    JSONObject distance = step.getJSONObject("distance");
                                                    JSONObject duration = step.getJSONObject("duration");
                                                    JSONObject start_location = step.getJSONObject("start_location");
                                                    JSONObject end_location = step.getJSONObject("end_location");

                                                    PointData data = new PointData();
                                                    data.setDistance(distance.getString("text"));
                                                    data.setDuration(duration.getString("text"));
                                                    data.setStart(new double[]{start_location.getDouble("lat"), start_location.getDouble("lng")});
                                                    data.setStart(new double[]{end_location.getDouble("lat"), end_location.getDouble("lng")});
                                                    pointDatas.add(data);
                                                }
                                            }
                                        }
                                        if(mMap!=null)
                                        {   for(PointData data: pointDatas)
                                        {   mMap.addCircle(
                                                new CircleOptions()
                                                        .center(new LatLng(data.getStart()[0], data.getStart()[1]))
                                                        .radius(4)
                                                        .visible(true)
                                                        .clickable(false)
                                                        .fillColor(Color.CYAN)
                                                        .strokeColor(Color.BLUE)
                                                        .strokeWidth(4));
                                        }
                                        }
                                    }
                                    catch (JSONException e)
                                    {   e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                    VolleySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
                }
                break;
            }
        }
    }
}
