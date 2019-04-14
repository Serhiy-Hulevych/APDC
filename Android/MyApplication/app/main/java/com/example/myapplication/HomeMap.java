package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Context;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Geocoder;
import android.location.Address;
import java.net.URL;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

public class HomeMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONObject location = null;
    private JSONArray res = null;
    private String lat = "";
    private String lng = "";
    private double latitude = 0;
    private double longitude = 0;
    private Context context;
    private UserDeleteTask deleteU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = this;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Button mEmailSignInButton = (Button) findViewById(R.id.logout);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectLogin(context);
            }
        });

        Button mAccount = (Button) findViewById(R.id.account);
        mAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectChangeSettings(context);
            }
        });

        Button mDelete = (Button) findViewById(R.id.delete);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser(context);
            }
        });
    }

    private void deleteUser(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            String parametro = "{ \"username\":"+"\""+prefs.getString("username","")+"\" }";
            JSONObject userDelete = new JSONObject(parametro);

            deleteU = new HomeMap.UserDeleteTask(this, userDelete);
            deleteU.execute((Void) null);
        } catch (Exception e) {
            System.out.print("Erro algures a criar o  user para apagar\n");
        }
    }

    private void  redirectLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.clear().commit();
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    private void  redirectChangeSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
        Intent intent = new Intent(context, ChangeSettingsActivity.class);
        startActivity(intent);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String morada = prefs.getString("address","");

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+morada+"&key=AIzaSyDiPZQSt7k2Hhi9YvZnpHl8ibIaL81fb24";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            location = response;

                            try {

                                res = location.getJSONArray("results");
                                for (int i = 0; i < res.length(); i++) {

                                    JSONObject c = res.getJSONObject(i);

                                    JSONObject loc = c.optJSONObject("geometry").optJSONObject("location");

                                    lat = loc.getString("lat");

                                    lng = loc.getString("lng");

                                }
                            } catch (Exception e) {
                                System.out.println("Address not found");
                            }

                            latitude = Double.parseDouble(lat);
                            longitude = Double.parseDouble(lng);

                            // Add a marker in Sydney and move the camera
                            LatLng useAddress = new LatLng(latitude, longitude);
                            mMap.addMarker(new MarkerOptions().position(useAddress).title("Marker in Address"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(useAddress.latitude, useAddress.longitude), 12.0f));

                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Volley error");

                    }
                });

            queue.add(request);

    }







    public class UserDeleteTask extends AsyncTask<Void, Void, String> {

        private JSONObject delete;
        private Context context;

        UserDeleteTask(Context context, JSONObject delete) {
            this.delete = delete;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                return RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/delete/v1"), delete);

            } catch (Exception e) {
                System.out.println("Erro algures no background\n");
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            redirectLogin(context);
        }
    }

}

