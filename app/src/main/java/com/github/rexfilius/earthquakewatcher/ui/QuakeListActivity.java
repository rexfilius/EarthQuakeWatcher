package com.github.rexfilius.earthquakewatcher.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rexfilius.earthquakewatcher.model.EarthQuake;
import com.example.earthquakewatcher.R;
import com.github.rexfilius.earthquakewatcher.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuakeListActivity extends AppCompatActivity {

    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue requestQueue;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);

        listView = findViewById(R.id.listView);
        requestQueue = Volley.newRequestQueue(this);
        arrayList = new ArrayList<>();

        getAllQuakes(Constants.URL);
    }

    public void getAllQuakes(String url) {
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                EarthQuake earthQuake = new EarthQuake();
                                try {
                                    JSONArray jsonArray = response.getJSONArray("features");
                                    for(int i=0; i<Constants.LIMIT; i++) {
                                        JSONObject properties =
                                                jsonArray.getJSONObject(i)
                                                        .getJSONObject("properties");
                                        JSONObject geometry =
                                                jsonArray.getJSONObject(i)
                                                        .getJSONObject("geometry");
                                        JSONArray coordinates =
                                                geometry.getJSONArray("coordinates");

                                        earthQuake.setPlace(properties.getString("place"));
                                        arrayList.add(earthQuake.getPlace());
                                    }

                                    arrayAdapter = new ArrayAdapter<>(QuakeListActivity.this,
                                            android.R.layout.simple_list_item_1,
                                            android.R.id.text1, arrayList);
                                    listView.setAdapter(arrayAdapter);
                                    arrayAdapter.notifyDataSetChanged();

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
}
