package com.example.vaccinenotifier;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.vaccinenotifier.NotifierService;
import com.example.vaccinenotifier.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotAvailableActivity extends Activity {

    ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> availSlotsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_slot);
        listView = findViewById(R.id.listView);
        availSlotsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, availSlotsList);
        listView.setAdapter(adapter);

        if(savedInstanceState != null) {
            if(savedInstanceState.getStringArray("availSlotsArray") != null) {
                String[] availSlotsArray = savedInstanceState.getStringArray("availSlotsArray");
                availSlotsList.clear();
                availSlotsList.addAll(Arrays.asList(availSlotsArray));
                adapter.notifyDataSetChanged();
            }
        }

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null) {
            if(extras.containsKey("availSlotsArray") && extras.get("availSlotsArray") instanceof Object[]) {
                Object[] availSlotsArray = (Object[]) extras.get("availSlotsArray");
                if(availSlotsArray.length > 0) {
                    availSlotsList.clear();
                    for(int i=0;i<availSlotsArray.length;i++) {
                        String slotInfo = availSlotsArray[i].toString();
                        availSlotsList.add(slotInfo);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "onNewIntent() availSlotsArray is empty", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
