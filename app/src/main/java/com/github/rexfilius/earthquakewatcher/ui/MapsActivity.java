package com.github.rexfilius.earthquakewatcher.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rexfilius.earthquakewatcher.model.EarthQuake;
import com.example.earthquakewatcher.R;
import com.github.rexfilius.earthquakewatcher.util.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue requestQueue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button showListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListButton = findViewById(R.id.showListButton);
        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, QuakeListActivity.class));
            }
        });

        requestQueue = Volley.newRequestQueue(this);
        getEarthQuakes();
    }

    public void getEarthQuakes() {
        final EarthQuake earthQuake = new EarthQuake();
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, Constants.URL,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray features = response.getJSONArray("features");
                                    for(int i=0; i<Constants.LIMIT; i++) {
                                        JSONObject properties = features.getJSONObject(i)
                                                                        .getJSONObject("properties");
                                        JSONObject geometry  = features.getJSONObject(i)
                                                                        .getJSONObject("geometry");
                                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                                        double longitude = coordinates.getDouble(0);
                                        double latitude  = coordinates.getDouble(1);

                                        earthQuake.setPlace(properties.getString("place"));
                                        earthQuake.setType(properties.getString("type"));
                                        earthQuake.setTime(properties.getLong("time"));
                                        earthQuake.setMagnitude(properties.getDouble("mag"));
                                        earthQuake.setDetailLink(properties.getString("detail"));

                                        java.text.DateFormat dateFormat =
                                                java.text.DateFormat.getDateInstance();
                                        String formattedDate =
                                                dateFormat.format(new Date(
                                                        properties.getLong("time"))
                                                        .getTime());

                                        MarkerOptions markerOptions = new MarkerOptions();
                                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN));
                                        markerOptions.title(earthQuake.getPlace());
                                        markerOptions.position(new LatLng(latitude, longitude));
                                        markerOptions.snippet(
                                                "Magnitude: " + earthQuake.getMagnitude() +
                                                "\n" + "Date: " + formattedDate);

                                        if(earthQuake.getMagnitude() >3.0) {
                                            markerOptions.icon(
                                                    BitmapDescriptorFactory.defaultMarker(
                                                            BitmapDescriptorFactory.HUE_RED));
                                        }

                                        Marker marker = mMap.addMarker(markerOptions);
                                        marker.setTag(earthQuake.getDetailLink());
                                        mMap.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                        new LatLng(longitude, latitude), 1));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonObjectRequest);
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
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else  {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0 ,
                    0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);

            Location location = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getQuakeDetails(marker.getTag().toString());
    }

    public void getQuakeDetails(String url) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                String detailsUrl = "";
                                try {
                                    JSONObject properties = response.getJSONObject("properties");
                                    JSONObject products = properties.getJSONObject("products");
                                    JSONArray geoserve = products.getJSONArray("geoserve");
                                    for(int i=0; i<geoserve.length(); i++) {
                                        JSONObject geoserveObject = geoserve.getJSONObject(i);
                                        JSONObject contentObject =
                                                geoserveObject.getJSONObject("contents");
                                        JSONObject geoJsonObject =
                                                contentObject.getJSONObject("geoserve.json");
                                        detailsUrl = geoJsonObject.getString("url");
                                    }
                                    getMoreDetails(detailsUrl);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    public void getMoreDetails(String url) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                                View view  = getLayoutInflater().inflate(
                                        R.layout.popup, null);
                                Button dismissButton = view.findViewById(R.id.dismissPopup);
                                Button dismissButtonTop = view.findViewById(R.id.dismissPopupTop);
                                TextView popupList = view.findViewById(R.id.popupList);
                                StringBuilder stringBuilder = new StringBuilder();

                                try {
                                    JSONArray cities = response.getJSONArray("cities");
                                    for(int i=0; i<cities.length(); i++) {
                                        JSONObject citiesObject = cities.getJSONObject(i);
                                        stringBuilder.append(
                                                "City: " + citiesObject.getString("name")
                                                + "\n"
                                                + "Distance: " + citiesObject.getString("distance")
                                                + "\n"
                                                + "Population: " + citiesObject.getString("population"));
                                        stringBuilder.append("\n\n");
                                    }
                                    popupList.setText(stringBuilder);
                                    dismissButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialogBuilder.setView(view);
                                    dialog = dialogBuilder.create();
                                    dialog.show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
