package com.example.safe.drivelert.EmergencyMode;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class RaspberryConnectionService extends Service {
    Boolean disconnected=false;
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
        if (broadcastReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
//Map the intent filter to the receiver
            registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Started B", Toast.LENGTH_SHORT).show();
            Log.d("Toast :", "tttt");
            //sendBroadcast(disconnected);
        }
    };

    private void sendBroadcast(Boolean disconnected) {
        sendBroadcast(disconnected);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service", "onDestroy");
//STEP3: Unregister the receiver
        unregisterReceiver(broadcastReceiver);
    }


}
