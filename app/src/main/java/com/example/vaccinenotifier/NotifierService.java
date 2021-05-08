package com.example.vaccinenotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.vaccinenotifier.data.model.UserParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.vaccinenotifier.App.CHANNEL_ID;

public class NotifierService extends Service {

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    RequestQueue requestQueue;
    public static int SLEEPTIME = 1000*5;
    public static final String NOTIFICATION_CHANNEL_ID = "vaccine.slot.available.notification";
    public static final String channelName = "vaccineNotificationChannel";
    public static final int NOTIF_ID = 111001100;
    private Context ctx;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.

            UserParams userParams = (UserParams) msg.obj;

            Log.println(Log.INFO, "VaccineNotifierService", "selectedState: "+userParams.getSelectedState());
            Log.println(Log.INFO, "VaccineNotifierService", "selectedCity: "+userParams.getSelectedCity());
            Log.println(Log.INFO, "VaccineNotifierService", "selectedAge: "+userParams.getSelectedAge());

            //Poll server every 10 mins and print result in logs
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String date = sdf.format(new Date());

            List<String> availSlots = new ArrayList<>();
            int ageLimit = 60;
            switch(userParams.getSelectedAge()) {
                case "18-45 only":
                    ageLimit = 18;
                    break;
                case "All age group":
                    ageLimit = 45;
                    break;
                default:
                    break;
            }

            while(availSlots.size() < 1) {

                //API to get availability by district for next 7 days
                //https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=670&date=07-05-2021

                String districtCalUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="
                        +userParams.getSelectedCityId()+"&date="+date;

                int finalAgeLimit = ageLimit;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, districtCalUrl, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {

                                Toast.makeText(getApplicationContext(), "got district calendar resp", Toast.LENGTH_SHORT).show();
                                Log.println(Log.INFO, "VaccineNotifierService", "Got response from url: "+districtCalUrl);
                                try {
                                    JSONArray centersJson = (JSONArray) response.get("centers");
                                    for(int i=0; i<centersJson.length();i++) {
                                        JSONObject centerJson = centersJson.getJSONObject(i);
                                        String centerName = centerJson.getString("name");
                                        String centerAddr = centerJson.getString("address");
                                        String centerFeeType = centerJson.getString("fee_type");
                                        String pincode = centerJson.getString("pincode");

                                        JSONArray sessionsJson = centerJson.getJSONArray("sessions");

                                        for(int j=0;j<sessionsJson.length();j++) {
                                            JSONObject sessionJson = sessionsJson.getJSONObject(j);
                                            int availCapacity = sessionJson.getInt("available_capacity");
                                            int minAgeLimit = sessionJson.getInt("min_age_limit");
                                            String date = sessionJson.getString("date");
                                            String vaccine = sessionJson.getString("vaccine");

                                            if(availCapacity > 0 && minAgeLimit <= finalAgeLimit) {
                                                availSlots.add(centerName+":"+centerAddr+":"+" Age="+minAgeLimit+"; Capacity="+availCapacity+"; Date="+date+"; Vaccine="+vaccine);
                                                Log.println(Log.INFO, "VaccineNotifierService", "############ SLOT FOUND #################");
                                                Log.println(Log.INFO, "VaccineNotifierService", centerName+":"+centerAddr+":"+" Age="+minAgeLimit+"; Capacity="+availCapacity+"; Date="+date+"; Vaccine="+vaccine);
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("VaccineNotifierService", "JSON Error: "+e.toString());
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.e("VaccineNotifierService", "Volley Error: "+error.toString());
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

                requestQueue.add(jsonObjectRequest);

                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("VaccineNotifierService", "Thread sleep interrupted: "+e.toString());
                }

            }

            //TODO Notify the user of available slot
            if (availSlots.size() > 1) {
                Intent notificationIntent = new Intent(getApplicationContext(), SlotAvailableActivity.class);
                notificationIntent.putExtra("availSlotsArray", availSlots.toArray());
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(NotifierService.this, NOTIFICATION_CHANNEL_ID);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Available vaccine slot found")
                        .setContentText(userParams.getSelectedState()+"-"+userParams.getSelectedCity()+"-"+userParams.getSelectedAge())
                        .setSubText("vaccine slot found")
                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(NOTIF_ID, notification);
            }

        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        requestQueue = Volley.newRequestQueue(this);
        ctx = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;

        UserParams userParams = new UserParams();
        userParams.setSelectedState(intent.getStringExtra("selectedState"));
        userParams.setSelectedStateId(intent.getIntExtra("selectedStateId", 0));
        userParams.setSelectedCity(intent.getStringExtra("selectedCity"));
        userParams.setSelectedCityId(intent.getIntExtra("selectedCityId", 0));
        userParams.setSelectedAge(intent.getStringExtra("selectedAge"));

        msg.obj = userParams;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setContentTitle("Checking available slots every 10 mins")
                .setContentText(userParams.getSelectedState()+"-"+userParams.getSelectedCity()+"-"+userParams.getSelectedAge())
                .setSubText("checking slots")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground(userParams);
        else
            startForeground(NOTIF_ID, notification);

        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_REDELIVER_INTENT;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(UserParams userParams){
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Checking available slots every 10 mins")
                .setContentText(userParams.getSelectedState()+"-"+userParams.getSelectedCity()+"-"+userParams.getSelectedAge())
                .setSubText("checking slots")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        startForeground(NOTIF_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
