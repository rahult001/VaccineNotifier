package com.example.vaccinenotifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.vaccinenotifier.databinding.ActivityLoginBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityLoginBinding binding;
    private Map<String, Integer> stateMap = new HashMap<>();
    private Map<String, Integer> districtMap = new HashMap<>();
    String[] states;
    String[] districts;
    private Context ctx;
    RequestQueue requestQueue;
    String selectedState;
    String selectedCity;
    Integer selectedStateId;
    Integer selectedCityId;
    String selectedAge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityLoginBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());


        ctx = this.getApplicationContext();
        final Spinner state_spinner = findViewById(R.id.state);
        final Spinner city_spinner = findViewById(R.id.city);
        final Spinner age_spinner =  findViewById(R.id.age);
        final Button notify_btn = findViewById(R.id.notify);
        final Button stop_notify_btn = findViewById(R.id.stop_notify);

        // Creates the Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        stateMap.put("Andaman and Nicobar Islands", 1);
        stateMap.put("Andhra Pradesh", 2);
        stateMap.put("Arunachal Pradesh", 3);
        stateMap.put("Assam", 4);
        stateMap.put("Bihar", 5);
        stateMap.put("Chandigarh", 6);
        stateMap.put("Chhattisgarh", 7);
        stateMap.put("Dadra and Nagar Haveli", 8);
        stateMap.put("Daman and Diu", 37);
        stateMap.put("Delhi", 9);
        stateMap.put("Goa", 10);
        stateMap.put("Gujarat", 11);
        stateMap.put("Haryana", 12);
        stateMap.put("Himachal Pradesh", 13);
        stateMap.put("Jammu and Kashmir", 14);
        stateMap.put("Jharkhand", 15);
        stateMap.put("Karnataka", 16);
        stateMap.put("Kerala", 17);
        stateMap.put("Ladakh", 18);
        stateMap.put("Lakshadweep", 19);
        stateMap.put("Madhya Pradesh", 20);
        stateMap.put("Maharashtra", 21);
        stateMap.put("Manipur", 22);
        stateMap.put("Meghalaya", 23);
        stateMap.put("Mizoram", 24);
        stateMap.put("Nagaland", 25);
        stateMap.put("Odisha", 26);
        stateMap.put("Puducherry", 27);
        stateMap.put("Punjab", 28);
        stateMap.put("Rajasthan", 29);
        stateMap.put("Sikkim", 30);
        stateMap.put("Tamil Nadu", 31);
        stateMap.put("Telangana", 32);
        stateMap.put("Tripura", 33);
        stateMap.put("Uttar Pradesh", 34);
        stateMap.put("Uttarakhand", 35);
        stateMap.put("West Bengal", 36);

        states = stateMap.keySet().toArray(new String[0]);

        ArrayAdapter<String> state_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, states);
        state_spinner.setAdapter(state_adapter);

        //Populate city base on selected state
        state_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = parent.getItemAtPosition(position).toString();
                selectedStateId = stateMap.get(selectedState);

                Log.println(Log.INFO, "VaccineNotifierrrrr", "selectedState: "+selectedState+", selectedStateId: "+selectedStateId);

                //Call endpoint https://cdn-api.co-vin.in/api/v2/admin/location/districts/{state_id}
                //and populate city spinner based on district_id, district_name
                String district_url = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/"+selectedStateId;

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, district_url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                districtMap = new HashMap<>();
                                try {
                                    JSONArray districtsJson = (JSONArray) response.get("districts");
                                    for(int i=0; i<districtsJson.length();i++) {
                                        JSONObject obj = districtsJson.getJSONObject(i);
                                        districtMap.put(obj.getString("district_name"), obj.getInt("district_id"));
                                    }

                                    districts = districtMap.keySet().toArray(new String[0]);

                                    ArrayAdapter<String> district_adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, districts);
                                    city_spinner.setAdapter(district_adapter);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("JSON Parse Error", e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.e("Volley", "Error");
                                Log.e("Volley", error.toString());
                            }
                        }) {

                    /**
                     * Passing some request headers
                     */
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("accept", "application/json");
                        headers.put("User-Agent", "Mozilla/5.0");
                        headers.put("Accept-Language", "en_US");
                        return headers;
                    }
                };

                //SingletonConnection.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
                requestQueue.add(jsonObjectRequest);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        city_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = parent.getItemAtPosition(position).toString();
                selectedCityId = districtMap.get(selectedCity);

                Log.println(Log.INFO, "VaccineNotifierrrrr", "selectedCity: "+selectedCity+", selectedCityId: "+selectedCityId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<CharSequence> age_adapter = ArrayAdapter.createFromResource(this,
                R.array.age_array, android.R.layout.simple_spinner_dropdown_item);
        age_spinner.setAdapter(age_adapter);
        age_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAge = (String) parent.getItemAtPosition(position);
                Log.println(Log.INFO, "VaccineNotifierrrrr", "selectedAge: "+selectedAge);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        notify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(ctx, NotifierService.class);
                serviceIntent.putExtra("selectedStateId", selectedStateId);
                serviceIntent.putExtra("selectedState", selectedState);
                serviceIntent.putExtra("selectedCityId", selectedCityId);
                serviceIntent.putExtra("selectedCity", selectedCity);
                serviceIntent.putExtra("selectedAge", selectedAge);
                ContextCompat.startForegroundService(ctx, serviceIntent);
            }
        });

        stop_notify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.stopService(new Intent(ctx, NotifierService.class));
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}